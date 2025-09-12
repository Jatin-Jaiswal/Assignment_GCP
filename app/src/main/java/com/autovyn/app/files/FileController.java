package com.autovyn.app.files;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/v1/files")
public class FileController {

    @Value("${app.uploadDir:uploads}")
    private String uploadDir;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> upload(@RequestParam("file") MultipartFile file) throws IOException {
        File dir = new File(uploadDir);
        if (!dir.exists() && !dir.mkdirs()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "failed to create upload dir"));
        }
        String id = UUID.randomUUID().toString();
        String safeName = id + "-" + file.getOriginalFilename();
        File out = new File(dir, safeName);
        try (FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(file.getBytes());
        }
        long expiresAt = Instant.now().plusSeconds(600).toEpochMilli();
        String token = signPath(safeName, expiresAt);
        String url = "/v1/files/" + safeName + "?expires=" + expiresAt + "&token=" + token;
        return ResponseEntity.ok(Map.of("id", id, "name", file.getOriginalFilename(), "size", file.getSize(), "downloadUrl", url));
    }

    @GetMapping("/{name}")
    public ResponseEntity<byte[]> download(@PathVariable("name") String name, @RequestParam("expires") long expires, @RequestParam("token") String token) throws IOException {
        if (Instant.now().toEpochMilli() > expires) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        String expected = signPath(name, expires);
        if (!expected.equals(token)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        File file = new File(uploadDir, name);
        if (!file.exists()) return ResponseEntity.notFound().build();
        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] bytes = StreamUtils.copyToByteArray(fis);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentLength(bytes.length);
            headers.set(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"");
            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        }
    }

    private String signPath(String name, long expires) throws IOException {
        String secret = readOrCreateSecret();
        String input = name + ":" + expires + ":" + secret;
        byte[] md5 = DigestUtils.md5Digest(input.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(md5);
    }

    private String readOrCreateSecret() throws IOException {
        File secretFile = new File(uploadDir, ".secret");
        if (!secretFile.getParentFile().exists()) {
            secretFile.getParentFile().mkdirs();
        }
        if (!secretFile.exists()) {
            Files.writeString(secretFile.toPath(), UUID.randomUUID().toString());
        }
        return Files.readString(secretFile.toPath()).trim();
    }
}


