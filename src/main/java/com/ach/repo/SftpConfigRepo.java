package com.ach.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ach.model.SftpConfig;

public interface SftpConfigRepo extends JpaRepository<SftpConfig, String> {
	Optional<SftpConfig> findByClientKey(String clientKey);
}
