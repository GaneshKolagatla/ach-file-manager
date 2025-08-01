package com.ach.repo;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ach.model.SftpClient;

public interface SftpClientRepo extends JpaRepository<SftpClient,Long>{
	
}
