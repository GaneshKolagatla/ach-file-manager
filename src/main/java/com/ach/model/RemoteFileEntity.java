//package com.ach.model;
//
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import jakarta.persistence.Column;
//import jakarta.persistence.Entity;
//import jakarta.persistence.GeneratedValue;
//import jakarta.persistence.GenerationType;
//import jakarta.persistence.Id;
//import jakarta.persistence.Table;
//import lombok.Data;
//
//@Entity
//@Table(name = "remote_file_table")
//@Data
//public class RemoteFileEntity {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long remoteFileId;
//
//    @Column(nullable = false)
//    private UUID fileId;
//
//    @Column(nullable = false)
//    private String fileName;
//
//    @Column(nullable = false)
//    private String fileLocation;
//
//    @Column(nullable = false)
//    private String eventType;
//
//    @Column(nullable = false, updatable = false)
//    private LocalDateTime creationTime = LocalDateTime.now();
//
//    @Column(nullable = false)
//    private LocalDateTime lastModifiedTime = LocalDateTime.now();
//
//    // Getters and Setters
//}
