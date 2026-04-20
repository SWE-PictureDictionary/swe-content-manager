package com.swe.project.contentmanager.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.swe.project.contentmanager.dto.CreateTopicRequest;

@Component
public class ContentAccessClient 
{

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${content.access.url:http://localhost:8083}")
    private String contentAccessUrl;

    public ResponseEntity<String> getAllTopics() {
        String url = contentAccessUrl + "/topics";

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );
    }

    public ResponseEntity<String> getTopic(String id) {
        String url = contentAccessUrl + "/topics/" + id;

        return restTemplate.exchange(
                url,
                HttpMethod.GET,
                null,
                String.class
        );
    }

    public ResponseEntity<String> createTopic(CreateTopicRequest request) 
    {
        String url = contentAccessUrl + "/topics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateTopicRequest> entity = new HttpEntity<>(request, headers);

        return restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
        );
    }
}