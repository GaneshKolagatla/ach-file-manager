package com.ach.model;

import jakarta.persistence.Id;

public class SftpClient {
	
	@Id
	Integer Id;
	String host;
	String username;
	String password;
	String remoteDirectory;
	String port;
	
}
