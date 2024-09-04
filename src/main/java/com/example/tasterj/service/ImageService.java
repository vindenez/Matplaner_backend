package com.example.tasterj.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.UUID;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
public class ImageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.key}")
    private String supabaseKey;

    @Value("${supabase.bucket}")
    private String bucketName;

    private final RestTemplate restTemplate;

    public ImageService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String uploadImage(MultipartFile imageFile) {
        try {
            String filename = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
            String uploadUrl = UriComponentsBuilder.fromHttpUrl(supabaseUrl)
                    .pathSegment("storage", "v1", "object", bucketName, filename)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<byte[]> requestEntity = new HttpEntity<>(imageFile.getBytes(), headers);

            ResponseEntity<String> response = restTemplate.exchange(uploadUrl, HttpMethod.POST, requestEntity, String.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filename;
            } else {
                throw new RuntimeException("Failed to upload image: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image", e);
        }
    }
    public void deleteImage(String imageUrl) {
        try {
            String filename = extractFilenameFromUrl(imageUrl);

            String deleteUrl = UriComponentsBuilder.fromHttpUrl(supabaseUrl)
                    .pathSegment("storage", "v1", "object", bucketName, filename)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseKey);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(deleteUrl, HttpMethod.DELETE, requestEntity, String.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new RuntimeException("Failed to delete image: " + response.getBody());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image", e);
        }
    }

    private String extractFilenameFromUrl(String imageUrl) {
        return imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
    }

}
