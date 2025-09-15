package com.autovyn.app.files;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/files")
public class FileController {

    @Value("${app.gcs.bucket}")
    private String bucketName;

    private final Storage storage;

    public FileController() {
        this.storage = StorageOptions.getDefaultInstance().getService();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String id = UUID.randomUUID().toString();
        String fileName = id + "-" + file.getOriginalFilename();
        
        BlobId blobId = BlobId.of(bucketName, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType(file.getContentType())
                .build();
        
        Blob blob = storage.create(blobInfo, file.getBytes());
        
        // For now, return a simple response without signed URL
        return ResponseEntity.ok(Map.of(
            "id", id, 
            "name", file.getOriginalFilename(), 
            "size", file.getSize(), 
            "downloadUrl", "https://storage.googleapis.com/" + bucketName + "/" + fileName
        ));
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> download(@PathVariable("name") String name) throws IOException {
        BlobId blobId = BlobId.of(bucketName, name);
        Blob blob = storage.get(blobId);
        
        if (blob == null || !blob.exists()) {
            return ResponseEntity.notFound().build();
        }
        
        byte[] content = blob.getContent();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentLength(content.length);
        headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"");
        
        return new ResponseEntity<>(content, headers, HttpStatus.OK);
    }
}


