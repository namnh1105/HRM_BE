package com.hainam.worksphere.shared.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * Internal client for calling the Python Face Recognition API.
 * Used for face verification and face status checks.
 */
@Component
@Slf4j
public class FaceApiClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;
    private final ObjectMapper objectMapper;

    public FaceApiClient(
            @Value("${face-api.base-url:http://localhost:8000}") String baseUrl,
            ObjectMapper objectMapper
    ) {
        this.restTemplate = new RestTemplate();
        this.baseUrl = baseUrl;
        this.objectMapper = objectMapper;
    }

    /**
     * Verify if a face photo matches the given employee.
     *
     * @param photo      the face photo (JPEG/PNG)
     * @param employeeId the employee ID to verify against
     * @return true if the face matches the employee
     */
    public boolean verifyFace(MultipartFile photo, String employeeId) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", new ByteArrayResource(photo.getBytes()) {
                @Override
                public String getFilename() {
                    return photo.getOriginalFilename() != null ? photo.getOriginalFilename() : "face.jpg";
                }
            });
            body.add("employee_id", employeeId);

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/face/verify",
                    requestEntity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode data = root.path("data");
                return data.path("matched").asBoolean(false);
            }

            return false;
        } catch (RestClientException e) {
            log.error("Failed to call face verification API: {}", e.getMessage());
            throw new RuntimeException("Không thể kết nối đến dịch vụ nhận diện khuôn mặt", e);
        } catch (IOException e) {
            log.error("Failed to read photo file: {}", e.getMessage());
            throw new RuntimeException("Không thể đọc file ảnh khuôn mặt", e);
        }
    }

    /**
     * Check if an employee has a registered face.
     *
     * @param employeeId the employee ID
     * @return true if face is registered
     */
    public boolean isFaceRegistered(String employeeId) {
        try {
            ResponseEntity<String> response = restTemplate.getForEntity(
                    baseUrl + "/face/status/{employeeId}",
                    String.class,
                    employeeId
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                return root.path("data").path("registered").asBoolean(false);
            }
            return false;
        } catch (RestClientException e) {
            log.error("Failed to check face status: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.error("Unexpected error checking face status: {}", e.getMessage());
            return false;
        }
    }
}
