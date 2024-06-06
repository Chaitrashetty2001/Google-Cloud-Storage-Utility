package com.example.demo.dto;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "gcs")
public class GcsConfig {
	 private String bucketName;
	    private String serviceAccountKeyFilePath;
	    private String projectId;

	    public String getBucketName() {
	        return bucketName;
	    }

	    public void setBucketName(String bucketName) {
	        this.bucketName = bucketName;
	    }

	    public String getServiceAccountKeyFilePath() {
	        return serviceAccountKeyFilePath;
	    }

	    public void setServiceAccountKeyFilePath(String serviceAccountKeyFilePath) {
	        this.serviceAccountKeyFilePath = serviceAccountKeyFilePath;
	    }

	    public String getProjectId() {
	        return projectId;
	    }

	    public void setProjectId(String projectId) {
	        this.projectId = projectId;
	    }
	}


