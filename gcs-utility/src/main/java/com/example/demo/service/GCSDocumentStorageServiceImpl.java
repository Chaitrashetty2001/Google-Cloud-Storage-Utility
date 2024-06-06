package com.example.demo.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.dto.UploadFileRequest;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.ImmutableMap;

@Service
public class GCSDocumentStorageServiceImpl implements GCSDocumentStorageService {

	private static final Logger logger = LoggerFactory.getLogger(GCSDocumentStorageServiceImpl.class);

	private static final String CONFIG_FILE_PATH = "src/p3.properties";

	@Override
	public String gCSDocumentStore(String filePath, String key1, String key2, String key3) {

		Properties properties = new Properties();
		try (FileInputStream input = new FileInputStream("src/config.properties")) {
			properties.load(input);
		} catch (IOException e) {
			logger.error("Error reading configuration file: " + e.getMessage());
			System.exit(1);
		}

		String serviceAccountKeyFilePath = properties.getProperty("service_account_key_file_path");
		if (serviceAccountKeyFilePath == null || serviceAccountKeyFilePath.isEmpty()) {
			logger.error("Service account key file path is not specified.");
			System.exit(1);
		}

		try {
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new FileInputStream(serviceAccountKeyFilePath));
			Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

			String bucketName = properties.getProperty("bucket_name");
			java.nio.file.Path path = Paths.get(filePath);
			String fileName = path.getFileName().toString();
			String contentType = Files.probeContentType(path);

			// Rename metadata keys during conversion
			ImmutableMap<String, String> metadata = ImmutableMap.of("key1", key1, "key2", key2, "key3", key3);

			BlobInfo documentMetadata = BlobInfo.newBuilder(bucketName, fileName).setContentType(contentType)
					.setMetadata(metadata).build();

			Blob blob = storage.create(documentMetadata, Files.readAllBytes(path));
			logger.info("File " + fileName + " uploaded to " + bucketName + " with metadata.");
		} catch (IOException e) {
			logger.error("Error uploading file to GCS: " + e.getMessage());
		}
		return "File uploaded successfully";
	}

	@Override
	public String storeFile(String filePath, String retrievedFilePath) {
		Properties properties = new Properties();
		try (FileInputStream input = new FileInputStream("src/p2.properties")) {
			properties.load(input);
		} catch (Exception e) {
			logger.error("Error reading configuration file: " + e.getMessage());
			return "Error reading configuration file";
		}

		String url = properties.getProperty("database_url");
		String username = properties.getProperty("database_username");
		String password = properties.getProperty("database_password");
		String tableName = properties.getProperty("table_name");
		String blobColumnName = properties.getProperty("blob_column_name");
		String fileNameColumnName = properties.getProperty("file_name_column_name");
		String fileTypeColumnName = properties.getProperty("file_type_column_name");

		try {
			Connection connection = DriverManager.getConnection(url, username, password);

			String sql = "INSERT INTO " + tableName + " (" + blobColumnName + ", " + fileNameColumnName + ", "
					+ fileTypeColumnName + ") " + "VALUES (?, ?, ?)";
			PreparedStatement statement = connection.prepareStatement(sql);

			File file = new File(filePath);
			FileInputStream inputStream = new FileInputStream(file);

			String fileName = file.getName();
			String fileType = URLConnection.guessContentTypeFromName(filePath);

			statement.setBinaryStream(1, inputStream);
			statement.setString(2, fileName);
			statement.setString(3, fileType);

			statement.executeUpdate();

			logger.info("File saved/updated successfully!");

			ResultSet resultSet = statement.executeQuery("SELECT " + blobColumnName + " FROM " + tableName);

			if (resultSet.next()) {
				InputStream retrievedInputStream = resultSet.getBinaryStream(blobColumnName);
				File retrievedFile = new File(retrievedFilePath);
				FileOutputStream outputStream = new FileOutputStream(retrievedFile);

				byte[] buffer = new byte[4096];
				int bytesRead;

				while ((bytesRead = retrievedInputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, bytesRead);
				}

				logger.info("File retrieved successfully!");

				retrievedInputStream.close();
				outputStream.close();
			} else {
				logger.info("No file found in the database.");
			}

			inputStream.close();
			statement.close();
			connection.close();
		} catch (Exception e) {
			logger.error("Error accessing database: " + e.getMessage());
			return "Error accessing database";
		}

		return "File stored in database and retrieved in the desired location successfully";
	}

	public void uploadFilesToGCS() {
		Properties properties = new Properties();
		try (FileInputStream input = new FileInputStream(CONFIG_FILE_PATH)) {
			properties.load(input);
		} catch (IOException e) {
			logger.error("Error reading configuration file: " + e.getMessage());
			throw new RuntimeException("Error reading configuration file", e);
		}

		String serviceAccountKeyFilePath = properties.getProperty("service_account_key_file_path");
		if (serviceAccountKeyFilePath == null || serviceAccountKeyFilePath.isEmpty()) {
			logger.error("Service account key file path is not specified in the configuration file.");
			throw new RuntimeException("Service account key file path is not specified in the configuration file.");
		}

		String projectId = properties.getProperty("project_id");
		String bucketName = properties.getProperty("bucket_name");

		try {
			GoogleCredentials credentials = GoogleCredentials
					.fromStream(new FileInputStream(serviceAccountKeyFilePath));
			Storage storage = StorageOptions.newBuilder().setCredentials(credentials).setProjectId(projectId).build()
					.getService();

			String dbUrl = properties.getProperty("db_url");
			String username = properties.getProperty("db_username");
			String password = properties.getProperty("db_password");

			String tableName = properties.getProperty("table_name");
			String blobColumnName = properties.getProperty("blob_column_name");
			String fileNameColumnName = properties.getProperty("file_name_column_name");

			try (Connection connection = DriverManager.getConnection(dbUrl, username, password)) {
				String sql = "SELECT " + blobColumnName + ", " + fileNameColumnName + " FROM " + tableName;
				try (PreparedStatement statement = connection.prepareStatement(sql)) {
					try (ResultSet resultSet = statement.executeQuery()) {
						while (resultSet.next()) {
							InputStream inputStream = resultSet.getBinaryStream(blobColumnName);
							String fileName = resultSet.getString(fileNameColumnName);
							BlobId blobId = BlobId.of(bucketName, fileName);
							BlobInfo documentMetadata = BlobInfo.newBuilder(blobId).build();
							storage.create(documentMetadata, inputStream.readAllBytes());
							inputStream.close();
						}
						logger.info("Files uploaded to GCS successfully.");
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error: " + e.getMessage());
			throw new RuntimeException("Error uploading files to GCS", e);
		}
	}
	public void uploadMultipleFiles(List<UploadFileRequest> requests) {
        Properties properties = new Properties();
        try (FileInputStream input = new FileInputStream("src/config.properties")) {
            properties.load(input);
        } catch (IOException e) {
            logger.error("Error reading configuration file: " + e.getMessage());
            throw new RuntimeException(e);
        }

        String serviceAccountKeyFilePath = properties.getProperty("service_account_key_file_path");
        if (serviceAccountKeyFilePath == null || serviceAccountKeyFilePath.isEmpty()) {
            logger.error("Service account key file path is not specified.");
            throw new RuntimeException("Service account key file path is not specified.");
        }

        try {
            GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(serviceAccountKeyFilePath));
            Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
            String bucketName = properties.getProperty("bucket_name");

            for (UploadFileRequest request : requests) {
                String filePath = request.getFilePath();
                String fileName = Paths.get(filePath).getFileName().toString();
                String contentType = Files.probeContentType(Paths.get(filePath));
                byte[] fileContent = Files.readAllBytes(Paths.get(filePath));

                BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName)
                        .setContentType(contentType)
                        .setMetadata(ImmutableMap.of(
                                "key1", request.getKey1(),
                                "key2", request.getKey2(),
                                "key3", request.getKey3()
                        ))
                        .build();

                storage.create(blobInfo, fileContent);
                logger.info("File " + fileName + " uploaded to " + bucketName + " with metadata.");
            }
        } catch (IOException e) {
            logger.error("Error uploading files to GCS: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
	
	}

