package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class FileMetadataDTO {
    private String documentUrl;
    private String metadata;
    private String documentContent;

    public FileMetadataDTO(String documentUrl, String metadata, String documentContent) {
        this.documentUrl = documentUrl;
        this.metadata = metadata;
        this.documentContent = documentContent;
    }

//    // Getters and setters
//    public String getDocumentUrl() {
//        return documentUrl;
//    }
//
//    public void setDocumentUrl(String documentUrl) {
//        this.documentUrl = documentUrl;
//    }
//
//    public String getMetadata() {
//        return metadata;
//    }
//
//    public void setMetadata(String metadata) {
//        this.metadata = metadata;
//    }
//
//    public String getDocumentContent() {
//        return documentContent;
//    }
//
//    public void setDocumentContent(String documentContent) {
//        this.documentContent = documentContent;
//    }
}
