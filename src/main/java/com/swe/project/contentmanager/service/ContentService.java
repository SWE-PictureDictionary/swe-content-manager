package com.swe.project.contentmanager.service;

import com.swe.project.contentmanager.client.ContentAccessClient;
import com.swe.project.contentmanager.dto.CreateTopicRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class ContentService {

    private final ContentAccessClient client;

    public ContentService(ContentAccessClient client) {
        this.client = client;
    }

    public ResponseEntity<?> getAllTopics() {
        return client.getAllTopics();
    }

    public ResponseEntity<String> getTopic(String id) {
        return client.getTopic(id);
    }

    public ResponseEntity<String> createTopic(CreateTopicRequest request) {
        return client.createTopic(request);
    }
}