package com.ach.helper;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.security.Security;

import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.openpgp.PGPEncryptedData;
import org.bouncycastle.openpgp.PGPEncryptedDataList;
import org.bouncycastle.openpgp.PGPLiteralData;
import org.bouncycastle.openpgp.PGPObjectFactory;
import org.bouncycastle.openpgp.PGPPrivateKey;
import org.bouncycastle.openpgp.PGPPublicKeyEncryptedData;
import org.bouncycastle.openpgp.PGPSecretKey;
import org.bouncycastle.openpgp.PGPSecretKeyRingCollection;
import org.bouncycastle.openpgp.PGPUtil;
import org.bouncycastle.openpgp.operator.jcajce.JcaKeyFingerprintCalculator;
import org.bouncycastle.openpgp.operator.jcajce.JcePBESecretKeyDecryptorBuilder;
import org.bouncycastle.openpgp.operator.jcajce.JcePublicKeyDataDecryptorFactoryBuilder;

public class PGPDecryptor {

	static {
		Security.addProvider(new BouncyCastleProvider());
	}

	public static void decryptFile(File encryptedFile, File outputFile, InputStream privateKeyStream, char[] passphrase)
			throws Exception {
		try (InputStream decoderStream = PGPUtil.getDecoderStream(new FileInputStream(encryptedFile))) {
			PGPObjectFactory pgpFactory = new PGPObjectFactory(decoderStream, new JcaKeyFingerprintCalculator());
			Object pgpObject = pgpFactory.nextObject();

			PGPEncryptedDataList encList;
			if (pgpObject instanceof PGPEncryptedDataList) {
				encList = (PGPEncryptedDataList) pgpObject;
			} else {
				encList = (PGPEncryptedDataList) pgpFactory.nextObject();
			}

			PGPPrivateKey privateKey = null;
			PGPPublicKeyEncryptedData encryptedData = null;

			PGPSecretKeyRingCollection secretKeyRings = new PGPSecretKeyRingCollection(
					PGPUtil.getDecoderStream(privateKeyStream), new JcaKeyFingerprintCalculator());

			for (PGPEncryptedData ed : encList) {
				PGPPublicKeyEncryptedData pked = (PGPPublicKeyEncryptedData) ed;
				PGPSecretKey secretKey = secretKeyRings.getSecretKey(pked.getKeyID());
				if (secretKey != null) {
					privateKey = secretKey.extractPrivateKey(
							new JcePBESecretKeyDecryptorBuilder().setProvider("BC").build(passphrase));
					encryptedData = pked;
					break;
				}
			}

			if (privateKey == null || encryptedData == null) {
				throw new IllegalArgumentException("Private key not found or decryption failed.");
			}

			InputStream clear = encryptedData
					.getDataStream(new JcePublicKeyDataDecryptorFactoryBuilder().setProvider("BC").build(privateKey));

			PGPObjectFactory pgpFact = new PGPObjectFactory(clear, new JcaKeyFingerprintCalculator());
			Object message = pgpFact.nextObject();

			if (message instanceof PGPLiteralData) {
				PGPLiteralData literalData = (PGPLiteralData) message;
				InputStream in = literalData.getInputStream();
				Files.copy(in, outputFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
			} else {
				throw new IllegalArgumentException("Unexpected PGP message format.");
			}
		}
	}
}
