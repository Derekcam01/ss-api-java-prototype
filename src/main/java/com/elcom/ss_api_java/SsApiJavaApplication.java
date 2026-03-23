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

    @GetMapping("/")
    public String home() {
        String apiUrl = "https://www.find-tender.service.gov.uk/api/1.0/ocdsReleasePackages";

        try {
            // FIX: We build our 16MB web client directly inside the page loader!
            WebClient webClient = WebClient.builder()
                    .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                    .build();

            // Grab the data from the UK Gov
            String response = webClient.get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block(); 

            // Build a simple HTML webpage to display it
            String htmlPage = "<h1>Elcom FTS Data Portal</h1>";
            htmlPage += "<p><b>STATUS:</b> Connection Successful! Live data retrieved.</p><hr>";
            htmlPage += "<h3>Raw OCDS JSON Data (First 1500 characters):</h3>";
            htmlPage += "<pre style='background-color: #f4f4f4; padding: 15px; border-radius: 5px;'>" + response.substring(0, Math.min(response.length(), 1500)) + "...</pre>";

            return htmlPage;
            
        } catch (Exception e) {
            return "<h1>Error</h1><p>Connection failed: " + e.getMessage() + "</p>";
        }
    }
}