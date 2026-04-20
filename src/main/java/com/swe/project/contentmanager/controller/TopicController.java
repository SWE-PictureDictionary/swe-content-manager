package com.swe.project.contentmanager.controller;

import com.swe.project.contentmanager.dto.CreateTopicRequest;
import com.swe.project.contentmanager.service.ContentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/topics")
public class TopicController {

    private final ContentService service;

    public TopicController(ContentService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getAllTopics() {
        return service.getAllTopics();
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTopic(@PathVariable String id) {
        return service.getTopic(id);
    }

    @PostMapping
    public ResponseEntity<String> createTopic(@RequestBody CreateTopicRequest request) {
        return service.createTopic(request);
    }
}