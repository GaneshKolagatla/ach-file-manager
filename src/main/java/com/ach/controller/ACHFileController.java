package com.ach.controller;

import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ach.dto.ACHFileRequest;
import com.ach.dto.DownloadDto;
import com.ach.dto.PaymentInstruction;
import com.ach.model.SftpConfig;
import com.ach.repo.SftpConfigRepo;
import com.ach.service.PGPEncryptionService;
import com.ach.service.SftpUploadService;

@RestController
@RequestMapping("/generate-ach")
public class ACHFileController {

	@Autowired
	SftpConfigRepo repo;

	@Autowired
	private PGPEncryptionService encryptionService;

	@Autowired
	private SftpUploadService sftpUploadService;

	@Autowired
	private com.ach.service.SftpDownloadService sftpDownloadService;

	private static final String PRIVATE_KEY_PATH = "keys/vatsalya_private.asc"; // in resources
	private static final String DOWNLOAD_DIR = "target/decrypted-ach";
	private static final String PASSPHRASE = "test123";

	private static final String OUTPUT_DIR = "target/ach-files";
	private static final String PUBLIC_KEY_PATH = "keys/vatsalya_public.asc"; // placed in src/main/resources/keys/

	@PostMapping("/genrate")
	public String generateAndEncryptACHFile(@RequestBody ACHFileRequest request) {
		try {
			List<PaymentInstruction> instructions = request.getPaymentInstructions();

			// === Build ACH file content ===
			StringBuilder sb = new StringBuilder();
			int recordCount = 0;

			sb.append(buildFileHeader(request)).append("\n");
			recordCount++;

			sb.append(buildBatchHeader(request)).append("\n");
			recordCount++;

			for (PaymentInstruction pi : instructions) {
				sb.append(buildEntryDetail(pi)).append("\n");
				recordCount++;
			}

			sb.append(buildFileControl(recordCount)).append("\n");

			// === File naming ===
			String cleanBankName = request.getDestinationName().replaceAll("\\s+", "").toLowerCase();
			String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
			String filename = String.format("%s%s%s.ach", cleanBankName, request.getImmediateOrigin(), timestamp);

			// === Paths ===
			Path outputDir = Paths.get(OUTPUT_DIR);
			Files.createDirectories(outputDir);

			Path achFilePath = outputDir.resolve(filename);
			Files.writeString(achFilePath, sb.toString());

			// === Encrypt the ACH file ===
			String encryptedFileName = filename + ".pgp";
			Path encryptedFilePath = outputDir.resolve(encryptedFileName);

			try (InputStream publicKeyStream = new ClassPathResource(PUBLIC_KEY_PATH).getInputStream()) {
				File encryptedFile = encryptionService.encryptACHFile(achFilePath.toFile(), encryptedFilePath.toFile(),
						publicKeyStream);

				// === Upload encrypted file to SFTP based on clientKey ===
				sftpUploadService.uploadFile(request.getClientKey(), encryptedFile);

				return "ACH file generated, encrypted, and uploaded successfully: " + encryptedFileName;
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Failed: " + e.getMessage();
		}
	}

	@PostMapping("/download")
	public String downloadAndDecryptACHFile(@RequestBody DownloadDto requestt) {
		SftpConfig request = repo.findByClientKey(requestt.getClient_key()).orElseThrow(
				() -> new RuntimeException("SFTP config not found for client key: " + requestt.getClient_key()));

		try {

			Files.createDirectories(Paths.get(DOWNLOAD_DIR));
			String fileName = requestt.getFile_name(); // You’ll need to add this in your `ACHFileRequest` DTO
			File encryptedFile = Paths.get(DOWNLOAD_DIR, fileName).toFile();

			// Step 1: Download file from SFTP
			sftpDownloadService.downloadFile(request.getClientKey(), fileName, encryptedFile);

			// Step 2: Decrypt
			String decryptedFileName = fileName.replace(".pgp", ".ach");
			File decryptedFile = Paths.get(DOWNLOAD_DIR, decryptedFileName).toFile();

			try (InputStream privateKeyStream = new ClassPathResource(PRIVATE_KEY_PATH).getInputStream()) {
				encryptionService.decryptACHFile(encryptedFile, decryptedFile, privateKeyStream,
						PASSPHRASE.toCharArray());
			}

			return "File downloaded and decrypted successfully: " + decryptedFileName;
		} catch (Exception e) {
			e.printStackTrace();
			return "Download/Decryption failed: " + e.getMessage();
		}
	}

	// === Helper Methods ===

	private String buildFileHeader(ACHFileRequest req) {
		return pad("1", 1) + pad("01", 2) + pad(req.getImmediateDestination(), 10) + pad(req.getImmediateOrigin(), 10)
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMdd"))
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("HHmm")) + "A" + "094" + "10" + "1"
				+ pad(req.getDestinationName(), 23) + pad(req.getOriginName(), 23) + pad("", 8);
	}

	private String buildBatchHeader(ACHFileRequest req) {
		return pad("5", 1) + pad(req.getCompanyName(), 16) + pad("", 20) + pad(req.getCompanyId(), 10)
				+ pad(req.getEntryDescription(), 10) + pad("", 6) + pad("", 3) + "1"
				+ pad(req.getImmediateOrigin().substring(0, 8), 8) + pad("0000001", 7);
	}

	private String buildEntryDetail(PaymentInstruction pi) {
		return pad("6", 1) + pad(pi.getTransactionCode(), 2) + pad(pi.getReceivingDFIRouting().substring(0, 8), 8)
				+ pad(pi.getReceivingDFIRouting().substring(8), 1) + pad(pi.getReceivingDFIAccount(), 17)
				+ pad(String.valueOf(pi.getAmount()), 10, true) + pad(pi.getIndividualIdNumber(), 15)
				+ pad(pi.getIndividualName(), 22) + pad("", 2) + "0" + pad("0000001", 7);
	}

	private String buildFileControl(int totalRecords) {
		int blockCount = (int) Math.ceil(totalRecords / 10.0);
		return pad("9", 1) + pad(String.valueOf(totalRecords), 6, true) + pad(String.valueOf(blockCount), 6, true)
				+ pad("0000001", 8) + pad("", 87);
	}

	private String pad(String input, int length) {
		return pad(input, length, false);
	}

	private String pad(String input, int length, boolean leftPadWithZeros) {
		if (input == null)
			input = "";
		if (input.length() > length)
			return input.substring(0, length);
		StringBuilder sb = new StringBuilder();
		int padLength = length - input.length();
		String padChar = leftPadWithZeros ? "0" : " ";
		for (int i = 0; i < padLength; i++)
			sb.append(padChar);
		sb.append(input);
		return leftPadWithZeros ? sb.toString() : input + sb.substring(0, padLength);
	}
}
