package com.autovyn.app.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/v1/validate")
public class ValidationController {

    @Value("${app.gcs.bucket:${GCS_BUCKET:}}")
    private String bucketName;

    @Value("${gcp.project-id:${GOOGLE_CLOUD_PROJECT:}}")
    private String projectId;

    @Value("${app.pubsub.topic:${PUBSUB_TOPIC:}}")
    private String topicId;

    private final Storage storage = StorageOptions.getDefaultInstance().getService();

    @PostMapping("/sample")
    public ResponseEntity<Map<String, Object>> publishSample() {
        try {
            String id = UUID.randomUUID().toString();
            String timestamp = Instant.now().toString();
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("id", id);
            payload.put("timestamp", timestamp);
            payload.put("schemaVersion", "1");
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("message", "hello-world");
            payload.put("payload", body);

            if (projectId == null || projectId.isEmpty() || topicId == null || topicId.isEmpty()) {
                return ResponseEntity.status(500).body(Map.of("error", "Missing projectId/topicId config"));
            }

            ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
            Publisher publisher = null;
            try {
                publisher = Publisher.newBuilder(topicName).build();
                String jsonStr = json(payload);
                ByteString data = ByteString.copyFromUtf8(jsonStr);
                PubsubMessage message = PubsubMessage.newBuilder().setData(data).build();
                publisher.publish(message).get();
            } finally {
                if (publisher != null) {
                    publisher.shutdown();
                }
            }
            return ResponseEntity.ok(Map.of("status", "queued", "id", id));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> listValidated() {
        if (bucketName == null || bucketName.isEmpty()) {
            return ResponseEntity.status(500).body(Map.of("error", "GCS bucket not configured"));
        }
        Iterable<Blob> blobs = storage.list(bucketName, Storage.BlobListOption.prefix("validated/")).iterateAll();
        List<Map<String, Object>> items = new ArrayList<>();
        for (Blob b : blobs) {
            if (b.isDirectory()) continue;
            Map<String, Object> m = new LinkedHashMap<>();
            m.put("name", b.getName());
            m.put("size", b.getSize());
            m.put("updated", b.getUpdateTime());
            items.add(m);
        }
        // newest first
        List<Map<String, Object>> sorted = items.stream()
                .sorted(Comparator.comparing((Map<String, Object> m) -> (Long)m.get("updated")).reversed())
                .limit(50)
                .collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("items", sorted));
    }

    private String json(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder();
        toJson(map, sb);
        return sb.toString();
    }

    @SuppressWarnings("unchecked")
    private void toJson(Object obj, StringBuilder sb) {
        if (obj == null) { sb.append("null"); return; }
        if (obj instanceof String) { sb.append('"').append(escape((String)obj)).append('"'); return; }
        if (obj instanceof Number || obj instanceof Boolean) { sb.append(obj.toString()); return; }
        if (obj instanceof Map) {
            sb.append('{');
            boolean first = true;
            for (Map.Entry<String, Object> e : ((Map<String, Object>) obj).entrySet()) {
                if (!first) sb.append(',');
                first = false;
                sb.append('"').append(escape(e.getKey())).append('"').append(':');
                toJson(e.getValue(), sb);
            }
            sb.append('}');
            return;
        }
        if (obj instanceof Iterable) {
            sb.append('[');
            boolean first = true;
            for (Object v : (Iterable<?>) obj) {
                if (!first) sb.append(',');
                first = false;
                toJson(v, sb);
            }
            sb.append(']');
            return;
        }
        sb.append('"').append(escape(String.valueOf(obj))).append('"');
    }

    private String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
