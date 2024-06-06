package com.example.demo.service;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.dto.DocumentWrapper;
import com.example.demo.dto.FileMetadataDTO;
import com.example.demo.dto.Metadata;
import com.google.api.gax.paging.Page;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

@Service
public class GCSDocumentRetrievalServiceImpl implements GCSDocumentRetrievalService {

	private static final Logger logger = LoggerFactory.getLogger(GCSDocumentRetrievalServiceImpl.class);
	private final Storage storage;
	private final String bucketName;
	@Value("${service.account.key.file.path}")
	private String credentialsLocation;

	public GCSDocumentRetrievalServiceImpl(@Value("${service.account.key.file.path}") String serviceAccountKeyFilePath,
			@Value("${project.id}") String projectId, @Value("${bucket.name}") String bucketName) throws IOException {
		GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyFilePath));
		storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build().getService();
		this.bucketName = bucketName;
	}

	public List<String> retrieveDocuments(DocumentWrapper documentWrapper) throws IOException {
		List<String> documentContents = new ArrayList<>();
		Map<String, Blob> metadataToBlobMap = new HashMap<>();

		// Iterate through all blobs in the bucket
		Iterable<Blob> blobs = storage.list(bucketName).iterateAll();
		for (Blob blob : blobs) {
			Map<String, String> blobMetadata = blob.getMetadata();
			if (blobMetadata != null) {
				String combinedKey = generateCombinedKey(blobMetadata);
				metadataToBlobMap.put(combinedKey, blob);
			}
		}

		// Check each requested document against existing blobs
		List<Metadata> documentKeysList = documentWrapper.getMetadataList();
		for (Metadata documentKey : documentKeysList) {
			String combinedKey = generateCombinedKey(documentKey);
			Blob blob = metadataToBlobMap.get(combinedKey);

			if (blob != null) {
				String documentContent = downloadDocument(blob);
				documentContents.add(documentContent);
				logger.info("Retrieved document content: " + documentContent);
			} else {
				logger.error("No document found for keys: key1={}, key2={}, key3={}", documentKey.getKey1(),
						documentKey.getKey2(), documentKey.getKey3());
			}
		}

		return documentContents;
	}

	private String generateCombinedKey(Map<String, String> metadata) {
		return metadata.get("key1") + "|" + metadata.get("key2") + "|" + metadata.get("key3");
	}

	private String generateCombinedKey(Metadata documentKey) {
		return documentKey.getKey1() + "|" + documentKey.getKey2() + "|" + documentKey.getKey3();
	}

	private String downloadDocument(Blob blob) throws IOException {
		byte[] contentBytes = blob.getContent();
		return new String(contentBytes, StandardCharsets.UTF_8);
	}

	public List<String> retrieveAllDocuments() throws IOException {
		List<String> documentContents = new ArrayList<>();
		Map<String, Blob> metadataToBlobMap = new HashMap<>();

		Iterable<Blob> blobs = storage.list(bucketName).iterateAll();
		for (Blob blob : blobs) {
			Map<String, String> blobMetadata = blob.getMetadata();
			if (blobMetadata != null) {
				String combinedKey = generateCombinedKey(blobMetadata);
				metadataToBlobMap.put(combinedKey, blob);
			}
		}

		for (Map.Entry<String, Blob> entry : metadataToBlobMap.entrySet()) {
			Blob blob = entry.getValue();
			String documentContent = downloadDocument(blob);
			documentContents.add(documentContent);
			logger.info("Retrieved document content: " + documentContent);
		}

		return documentContents;
	}

	public String getBlobNameFromMetadata(Metadata metadata) {
		Page<Blob> blobs = storage.list(bucketName);
		for (Blob blob : blobs.iterateAll()) {
			Map<String, String> blobMetadata = blob.getMetadata();
			if (blobMetadata != null) {
				boolean match = true;
				for (Map.Entry<String, String> entry : metadata.entrySet()) {
					if (!blobMetadata.containsKey(entry.getKey())
							|| !blobMetadata.get(entry.getKey()).equals(entry.getValue())) {
						match = false;
						break;
					}
				}
				if (match) {
					return blob.getName();
				}
			}
		}
		return null;
	}

	public String downloadDocumentContent(String blobName) {
		Blob blob = storage.get(bucketName, blobName);
		if (blob != null) {
			return new String(blob.getContent());
		} else {
			logger.error("Error: Unable to retrieve document from GCS.");
			return null;
		}
	}

	public void downloadFileFromUrl(String fileUrl, String downloadPath) throws IOException {
		// Decode the URL in case it's URL encode
		String decodedUrl = URLDecoder.decode(fileUrl, "UTF-8");

		// Extract bucket name and blob name from the URL
		String[] parts = decodedUrl.split("/");
		String bucketName = parts[3];
		String blobName = parts[4];

		// Download the file from GCS
		downloadFile(bucketName, blobName, downloadPath);
	}

	private void downloadFile(String bucketName, String blobName, String downloadPath) throws IOException {
		Blob blob = storage.get(bucketName, blobName);
		if (blob != null) {
			try (FileOutputStream out = new FileOutputStream(downloadPath)) {
				byte[] content = blob.getContent();
				out.write(content);
			}
		} else {
			throw new IOException("Blob not found: " + blobName);
		}
	}

	public List<FileMetadataDTO> listFilesWithMetadataAndContent() throws IOException {
		List<FileMetadataDTO> results = new ArrayList<>();

		Storage storage = StorageOptions.newBuilder()
				.setCredentials(ServiceAccountCredentials.fromStream(new FileInputStream(credentialsLocation))).build()
				.getService();

		Page<Blob> blobs = storage.list(bucketName);
		for (Blob blob : blobs.iterateAll()) {

			String metadata = blob.getMetadata() != null ? blob.getMetadata().toString() : "";

			String documentUrl = "https://storage.cloud.google.com/" + bucketName + "/" + blob.getName();
			// static vlaue need to be in one single file

			// Document content
			String documentContent = new String(blob.getContent());

			FileMetadataDTO dto = new FileMetadataDTO(documentUrl, metadata, documentContent);

			results.add(dto);

			logger.info("Document URL: {}", documentUrl);
			logger.info("Metadata: {}", metadata);
			logger.info("Document Content: {}", documentContent);
		}

		return results;
	}
}
