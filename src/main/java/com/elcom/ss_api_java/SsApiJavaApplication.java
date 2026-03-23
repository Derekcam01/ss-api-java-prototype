package com.elcom.ss_api_java;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;

@SpringBootApplication
@RestController 
public class SsApiJavaApplication {

    public static void main(String[] args) {
        SpringApplication.run(SsApiJavaApplication.class, args);
    }

    // We moved our raw FTS data to a dedicated background API channel!
    // The frontend UI will eventually "call" this channel to get its data.
    @GetMapping("/api/live-data")
    public String getFtsData() {
        String apiUrl = "https://www.find-tender.service.gov.uk/api/1.0/ocdsReleasePackages";

        try {
            WebClient webClient = WebClient.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build();

            return webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); 
            
        } catch (Exception e) {
            return "{\"error\": \"Connection failed: " + e.getMessage() + "\"}";
        }
    }
}