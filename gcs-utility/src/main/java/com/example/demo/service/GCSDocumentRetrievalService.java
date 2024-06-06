package com.example.demo.service;

import java.io.IOException;
import java.util.List;

import com.example.demo.dto.DocumentWrapper;
import com.example.demo.dto.FileMetadataDTO;
import com.example.demo.dto.Metadata;

public interface GCSDocumentRetrievalService {
	public List<String> retrieveDocuments(DocumentWrapper documentWrapper) throws IOException;

	public String downloadDocumentContent(String blobName);

	String getBlobNameFromMetadata(Metadata metadata);

	public void downloadFileFromUrl(String fileUrl, String downloadPath) throws IOException;

	public List<String> retrieveAllDocuments() throws IOException;

	public List<FileMetadataDTO> listFilesWithMetadataAndContent() throws IOException;

	

	

}
