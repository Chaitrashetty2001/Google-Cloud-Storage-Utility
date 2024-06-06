**Features:**
1. Upload Multiple Files to GCS:
    - Endpoint: `/storage/gcs/documents/put`
    - Description: Upload multiple files to GCS with specified metadata.
    - Request Body: List of `UploadFileRequest`.
2. Fetch Documents Based on Metadata:
    - Endpoint: `/storage/gcs/document/list`
    - Description: Retrieve documents from GCS based on specified metadata keys and values.
    - Request Body: `DocumentWrapper` containing document keys.
3. Download File from GCS:
    - Endpoint: `/storage/gcs/document/download`
    - Description: Download a file from GCS to a specified local path.
    - Request Body: `DownloadRequest` containing `fileUrl` and `downloadPath`.
4. Fetch All Documents from GCS:
    - Endpoint: `/storage/gcs/document/all`
    - Description: Retrieve all documents from GCS.
5. Migrate Files from Database to GCS:
    - Endpoint: `/storage/cloudsql/document/migrate`
    - Description: Upload files from the database to GCS.
6. Upload File to Database:
    - Endpoint: `/storage/cloudsql/upload`
    - Description: Upload a file to the database as a BLOB, with retrieval path specified.
    - Request Body: `FileUploadRequest` containing `filePath` and `retrievedFilePath`.
7. List Files with Metadata and Content from GCS:
    - Endpoint: `/storage/gcs/files`
    - Description: Retrieve document URLs, metadata, and content from GCS for the specified bucket.

**Technologies Used**
- **Spring Boot**: For building the RESTful API.
- **Google Cloud Storage**: For storing and managing files.
- **Swagger**: For API documentation and testing.
- **Java**: As the primary programming language.
