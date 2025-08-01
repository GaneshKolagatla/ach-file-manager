//package com.ach.service;
//import java.time.LocalDateTime;
//import java.util.UUID;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import com.ach.model.RemoteFileEntity;
//import com.ach.repo.RemoteFileEntityRepo;
//
//@Service
//public class RemoteFileService {
//
//    @Autowired
//    private RemoteFileEntityRepo repository;
//
//    public void logFileUpload(String fileName, String fileLocation, String eventType) {
//        RemoteFileEntity fileRecord = new RemoteFileEntity();
//        fileRecord.setFileId(UUID.randomUUID());
//        fileRecord.setFileName(fileName);
//        fileRecord.setFileLocation(fileLocation);
//        fileRecord.setEventType(eventType);
//        fileRecord.setCreationTime(LocalDateTime.now());
//        fileRecord.setLastModifiedTime(LocalDateTime.now());
//        repository.save(fileRecord);
//    }
//}
