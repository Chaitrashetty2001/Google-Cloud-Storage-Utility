package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class FileUploadRequest {
	private String filePath;
	private String retrievedFilePath;

	// Getters and setters
	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getRetrievedFilePath() {
		return retrievedFilePath;
	}

	public void setRetrievedFilePath(String retrievedFilePath) {
		this.retrievedFilePath = retrievedFilePath;
	}
}
