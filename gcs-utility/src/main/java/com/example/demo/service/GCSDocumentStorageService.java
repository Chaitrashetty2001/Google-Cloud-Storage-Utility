package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.dto.UploadFileRequest;

@Service
public interface GCSDocumentStorageService {

	String gCSDocumentStore(String filePath, String metadataKey1, String metadataKey2, String metadataKey3);
	// public String gCSDocumentStore(MultipartFile file);
	// void gCSDocumentStore(MultipartFile file, String metadataKey1, String
	// metadataKey2, String metadataKey3);

	String storeFile(String filePath, String retrievedFilePath);

	void uploadFilesToGCS();

	void uploadMultipleFiles(List<UploadFileRequest> requests);

}
