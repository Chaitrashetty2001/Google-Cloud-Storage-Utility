package com.example.demo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.DocumentWrapper;
import com.example.demo.dto.DownloadRequest;
import com.example.demo.dto.FileMetadataDTO;
import com.example.demo.dto.FileUploadRequest;
import com.example.demo.dto.UploadFileRequest;
import com.example.demo.service.GCSDocumentRetrievalService;
import com.example.demo.service.GCSDocumentStorageService;

import io.swagger.annotations.ApiParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/storage")

public class GCSController {

	@Autowired
	private GCSDocumentRetrievalService gcsservice;
	@Autowired
	private GCSDocumentStorageService gcsdocumentstorage;

	@Operation(summary = "Upload files to Google Cloud Storage", description = " With set of  metadata and giving the filepath as input")
	@ApiResponses(value = {
			@ApiResponse(responseCode = "200", description = "File uploaded successfully", content = @Content),
			@ApiResponse(responseCode = "500", description = "Internal server error", content = @Content) })
	@PostMapping("/gcs/document/put")
	public ResponseEntity<String> uploadFile(@RequestBody UploadFileRequest request) {
		String response = gcsdocumentstorage.gCSDocumentStore(request.getFilePath(), request.getKey1(),
				request.getKey2(), request.getKey3());
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Upload multiple files to Google Cloud Storage", description = "With a list of set of metadata and giving the file paths as input")
	@ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Files uploaded successfully"),
			@ApiResponse(responseCode = "500", description = "Internal server error") })
	@PostMapping("/gcs/documents/put")
	public ResponseEntity<String> uploadFiles(@RequestBody List<UploadFileRequest> requests) {
		gcsdocumentstorage.uploadMultipleFiles(requests);
		return ResponseEntity.ok("Files uploaded successfully.");
	}

	@Operation(summary = "Fetch documents", description = "Based on metadata keys and values given document is being fetched")
	@PostMapping("/gcs/document/list")
	public ResponseEntity<List<String>> fetchDocuments(
			@ApiParam(value = "Wrapper object containing document keys", required = true) @RequestBody DocumentWrapper documentWrapper) {
		try {
			List<String> documentContents = gcsservice.retrieveDocuments(documentWrapper);
			return ResponseEntity.ok(documentContents);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
		}
	}

	@Operation(summary = "Dowload the file from Google Cloud Storage", description = "Downloads the file into given download path by giving  file path of a file in bucket and desired download path")
	@PostMapping("/gcs/document/download")
	public ResponseEntity<String> downloadFile(
			@ApiParam(value = "JSON object containing fileUrl and downloadPath") @RequestBody DownloadRequest downloadRequest) {
		try {
			gcsservice.downloadFileFromUrl(downloadRequest.getFileUrl(), downloadRequest.getDownloadPath());
			return ResponseEntity.ok("File downloaded successfully.");
		} catch (IOException e) {
			return ResponseEntity.status(500).body("Failed to download file: " + e.getMessage());
		}
	}

	@Operation(summary = "Fetch all documents")
	@GetMapping("/gcs/document/all")
	public ResponseEntity<List<String>> fetchAllDocuments() {
		try {
			List<String> documents = gcsservice.retrieveAllDocuments();
			return ResponseEntity.ok(documents);
		} catch (IOException e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

	@Operation(summary = "Upload files from database to Google Cloud Storage")
	@PostMapping("/cloudsql/document/migrate")
	public String uploadFilesToGCS() {
		try {
			gcsdocumentstorage.uploadFilesToGCS();
			return "Files uploaded to GCS successfully.";
		} catch (Exception e) {
			return "Failed to upload files to GCS. Error: " + e.getMessage();
		}
	}

	@Operation(summary = "Upload file to database", description = "Uploading the file (of any type) to database as a BLOB file.Taking  filepath of the file that need to stored and retrive path to retrive the sent file in desired location as input")
	@PostMapping("/cloudsql/upload")
	public ResponseEntity<String> uploadFile(@RequestBody FileUploadRequest request) {
		String response = gcsdocumentstorage.storeFile(request.getFilePath(), request.getRetrievedFilePath());
		return ResponseEntity.ok(response);
	}

	@Operation(summary = "Read all the files and METADATA from GCS", description = "It is a get operation where for the specified bucket name in class level file it will retrieve the documentUrl, metadata and documentContent")
	@GetMapping("/gcs/files")
	public List<FileMetadataDTO> listFilesWithMetadataAndContent() throws IOException {
		return gcsservice.listFilesWithMetadataAndContent();
	}

}
