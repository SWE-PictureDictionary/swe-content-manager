package com.swe.project.contentmanager.client;

import com.swe.project.contentmanager.dto.CreateTopicRequest;
import com.swe.project.contentmanager.dto.HotspotRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class ContentAccessClient {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${content.access.url:http://localhost:8083}")
    private String contentAccessUrl;

    public ResponseEntity<String> getAllTopics() {
        String url = contentAccessUrl + "/topics";
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.GET, null, String.class));
    }

    public ResponseEntity<String> getTopic(String id) {
        String url = contentAccessUrl + "/topics/" + id;
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.GET, null, String.class));
    }

    public ResponseEntity<String> createTopic(CreateTopicRequest request) {
        String url = contentAccessUrl + "/topics";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<CreateTopicRequest> entity = new HttpEntity<>(request, headers);
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> addHotspot(String id, HotspotRequest request) {
        String url = contentAccessUrl + "/topics/" + id + "/hotspots";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<HotspotRequest> entity = new HttpEntity<>(request, headers);
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    public ResponseEntity<String> updateHotspot(String id, String label, HotspotRequest request) {
        String encodedLabel = UriUtils.encodePathSegment(label, StandardCharsets.UTF_8);
        String url = contentAccessUrl + "/topics/" + id + "/hotspots/" + encodedLabel;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<HotspotRequest> entity = new HttpEntity<>(request, headers);
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.PUT, entity, String.class));
    }

    public ResponseEntity<String> deleteHotspot(String id, int index) {
        String url = contentAccessUrl + "/topics/" + id + "/hotspots/" + index;
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.DELETE, null, String.class));
    }

    public ResponseEntity<String> deleteTopic(String id) {
        String url = contentAccessUrl + "/topics/" + id;
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.DELETE, null, String.class));
    }

    public ResponseEntity<String> uploadImage(String id, MultipartFile file) {
        String url = contentAccessUrl + "/topics/" + id + "/images";
        return uploadMultipart(url, file);
    }

    public ResponseEntity<String> uploadAudio(String id, MultipartFile file) {
        String url = contentAccessUrl + "/topics/" + id + "/audio";
        return uploadMultipart(url, file);
    }

    public ResponseEntity<byte[]> getImage(String filename) {
        String encodedFilename = UriUtils.encodePathSegment(filename, StandardCharsets.UTF_8);
        String url = contentAccessUrl + "/topics/media/images/" + encodedFilename;
        return cleanByteResponse(restTemplate.exchange(url, HttpMethod.GET, null, byte[].class));
    }

    public ResponseEntity<byte[]> getAudio(String filename) {
        String encodedFilename = UriUtils.encodePathSegment(filename, StandardCharsets.UTF_8);
        String url = contentAccessUrl + "/topics/media/audio/" + encodedFilename;
        return cleanByteResponse(restTemplate.exchange(url, HttpMethod.GET, null, byte[].class));
    }

    private ResponseEntity<String> uploadMultipart(String url, MultipartFile file) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();

        try {
            body.add("file", new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException("Could not read uploaded file", e);
        }

        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        return cleanStringResponse(restTemplate.exchange(url, HttpMethod.POST, entity, String.class));
    }

    private ResponseEntity<String> cleanStringResponse(ResponseEntity<String> response) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = response.getHeaders().getContentType();

        if (contentType != null) {
            headers.setContentType(contentType);
        } else {
            headers.setContentType(MediaType.APPLICATION_JSON);
        }

        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }

    private ResponseEntity<byte[]> cleanByteResponse(ResponseEntity<byte[]> response) {
        HttpHeaders headers = new HttpHeaders();
        MediaType contentType = response.getHeaders().getContentType();

        if (contentType != null) {
            headers.setContentType(contentType);
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        }

        return new ResponseEntity<>(response.getBody(), headers, response.getStatusCode());
    }
}