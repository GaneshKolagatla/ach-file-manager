package com.ach.service;

import java.io.File;
import java.io.InputStream;

import org.springframework.stereotype.Service;

import com.ach.helper.PGPDecryptor;
import com.ach.helper.PGPEncryptor;

@Service
public class PGPEncryptionService {

	public File encryptACHFile(File inputFile, File outputFile, InputStream publicKeyStream) throws Exception {
		// Encrypts inputFile using the given public key stream and writes to outputFile
		PGPEncryptor.encryptFile(inputFile, outputFile, publicKeyStream);
		return outputFile;
	}

	public File decryptACHFile(File encryptedFile, File outputFile, InputStream privateKeyStream, char[] passphrase)
			throws Exception {
		PGPDecryptor.decryptFile(encryptedFile, outputFile, privateKeyStream, passphrase);
		return outputFile;
	}
}
