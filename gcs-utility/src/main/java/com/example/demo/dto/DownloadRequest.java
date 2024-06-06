package com.example.demo.dto;

public class DownloadRequest {

        private String fileUrl;
        private String downloadPath;

        public String getFileUrl() {
            return fileUrl;
        }

        public void setFileUrl(String fileUrl) {
            this.fileUrl = fileUrl;
        }

        public String getDownloadPath() {
            return downloadPath;
        }

        public void setDownloadPath(String downloadPath) {
            this.downloadPath = downloadPath;
        }
    }

