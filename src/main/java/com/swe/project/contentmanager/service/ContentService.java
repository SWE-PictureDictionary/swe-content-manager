package com.swe.project.contentmanager.service;

import com.swe.project.contentmanager.client.ContentAccessClient;
import com.swe.project.contentmanager.dto.CreateTopicRequest;
import com.swe.project.contentmanager.dto.HotspotRequest;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ContentService {

    private final ContentAccessClient client;

    private String cachedTopics = "[]";
    private final Map<String, String> cachedTopicById = new ConcurrentHashMap<>();

    public ContentService(ContentAccessClient client) {
        this.client = client;
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackGetAllTopics")
    public ResponseEntity<String> getAllTopics() {
        ResponseEntity<String> response = client.getAllTopics();

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            cachedTopics = response.getBody();
        }

        return response;
    }

    public ResponseEntity<String> fallbackGetAllTopics(Throwable t) {
        System.out.println("ContentAccess fallback for getAllTopics: " + t.getMessage());
        return ResponseEntity.ok(cachedTopics);
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackGetTopic")
    public ResponseEntity<String> getTopic(String id) {
        ResponseEntity<String> response = client.getTopic(id);

        if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
            cachedTopicById.put(id, response.getBody());
        }

        return response;
    }

    public ResponseEntity<String> fallbackGetTopic(String id, Throwable t) {
        System.out.println("ContentAccess fallback for getTopic(" + id + "): " + t.getMessage());

        String cachedTopic = cachedTopicById.get(id);

        if (cachedTopic != null) {
            return ResponseEntity.ok(cachedTopic);
        }

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("{\"error\":\"Topic is not available and no cached copy exists.\"}");
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackWriteOperation")
    public ResponseEntity<String> createTopic(CreateTopicRequest request) {
        ResponseEntity<String> response = client.createTopic(request);

        if (response.getStatusCode().is2xxSuccessful()) {
            refreshTopicsCacheQuietly();
        }

        return response;
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackTopicWriteOperation")
    public ResponseEntity<String> addHotspot(String id, HotspotRequest request) {
        ResponseEntity<String> response = client.addHotspot(id, request);

        if (response.getStatusCode().is2xxSuccessful()) {
            refreshTopicCacheQuietly(id);
            refreshTopicsCacheQuietly();
        }

        return response;
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackTopicWriteOperationWithLabel")
    public ResponseEntity<String> updateHotspot(String id, String label, HotspotRequest request) {
        ResponseEntity<String> response = client.updateHotspot(id, label, request);

        if (response.getStatusCode().is2xxSuccessful()) {
            refreshTopicCacheQuietly(id);
            refreshTopicsCacheQuietly();
        }

        return response;
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackDeleteHotspot")
    public ResponseEntity<String> deleteHotspot(String id, int index) {
        ResponseEntity<String> response = client.deleteHotspot(id, index);

        if (response.getStatusCode().is2xxSuccessful()) {
            refreshTopicCacheQuietly(id);
            refreshTopicsCacheQuietly();
        }

        return response;
    }

    @CircuitBreaker(name = "contentAccess", fallbackMethod = "fallbackDeleteTopic")
    public ResponseEntity<String> deleteTopic(String id) {
        ResponseEntity<String> response = client.deleteTopic(id);

        if (response.getStatusCode().is2xxSuccessful()) {
            cachedTopicById.remove(id);
            refreshTopicsCacheQuietly();
        }

        return response;
    }

    public ResponseEntity<String> uploadImage(String id, MultipartFile file) {
        ResponseEntity<String> response = client.uploadImage(id, file);
        refreshTopicCacheQuietly(id);
        refreshTopicsCacheQuietly();
        return response;
    }

    public ResponseEntity<String> uploadAudio(String id, MultipartFile file) {
        ResponseEntity<String> response = client.uploadAudio(id, file);
        refreshTopicCacheQuietly(id);
        refreshTopicsCacheQuietly();
        return response;
    }

    public ResponseEntity<byte[]> getImage(String filename) {
        return client.getImage(filename);
    }

    public ResponseEntity<byte[]> getAudio(String filename) {
        return client.getAudio(filename);
    }

    public ResponseEntity<String> fallbackWriteOperation(CreateTopicRequest request, Throwable t) {
        return writeFallback(t);
    }

    public ResponseEntity<String> fallbackTopicWriteOperation(String id, HotspotRequest request, Throwable t) {
        return writeFallback(t);
    }

    public ResponseEntity<String> fallbackTopicWriteOperationWithLabel(String id, String label, HotspotRequest request, Throwable t) {
        return writeFallback(t);
    }

    public ResponseEntity<String> fallbackDeleteHotspot(String id, int index, Throwable t) {
        return writeFallback(t);
    }

    public ResponseEntity<String> fallbackDeleteTopic(String id, Throwable t) {
        return writeFallback(t);
    }

    private ResponseEntity<String> writeFallback(Throwable t) {
        System.out.println("ContentAccess write fallback triggered: " + t.getMessage());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                .body("{\"error\":\"Content service is temporarily unavailable. Please try again later.\"}");
    }

    private void refreshTopicsCacheQuietly() {
        try {
            ResponseEntity<String> response = client.getAllTopics();

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                cachedTopics = response.getBody();
            }
        } catch (Exception ignored) {
            //keep the previous cache if the refresh fails
        }
    }

    private void refreshTopicCacheQuietly(String id) {
        try {
            ResponseEntity<String> response = client.getTopic(id);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                cachedTopicById.put(id, response.getBody());
            }
        } catch (Exception ignored) {
            //keep the previous cached topic if the refresh fails.
        }
    }
}