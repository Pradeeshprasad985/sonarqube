package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class App {

    // Dedicated thread pool for managed asynchronous tasks
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(4);
    
    // Modern HTTP Client with timeout configurations
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static void main(String[] args) {
        System.out.println("[⚡] Starting Enterprise Data Processor Application...\n");

        // Mock list of URLs to process concurrently
        List<String> targetUrls = List.of(
            "https://typicode.com",
            "https://typicode.com",
            "https://typicode.com"
        );

        try {
            // Initiate parallel asynchronous tasks
            List<CompletableFuture<String>> futures = targetUrls.stream()
                .map(url -> CompletableFuture.supplyAsync(() -> fetchAndProcessData(url), EXECUTOR))
                .collect(Collectors.toList());

            // Combine all futures into a single execution blocker
            CompletableFuture<Void> allTasks = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
            
            // Wait for all HTTP fetches and processing to complete
            allTasks.join();
            
            System.out.println("\n[✓] All data processing jobs completed successfully.");
        } catch (Exception e) {
            System.err.println("[X] Critical application failure: " + e.getMessage());
        } finally {
            // Gracefully shut down the custom thread pool
            EXECUTOR.shutdown();
        }
    }

    /**
     * Fetches raw JSON string from a target endpoint and logs the localized thread action.
     */
    private static String fetchAndProcessData(String url) {
        String threadName = Thread.currentThread().getName();
        System.out.printf("[Job] Thread %s is initiating fetch for: %s%n", threadName, url);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Accept", "application/json")
                .GET()
                .build();

        try {
            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() == 200) {
                String body = response.body();
                // Extracting basic elements using standard string manipulation to mimic parsing
                String parsedTitle = extractJsonField(body, "title");
                System.out.printf("[Success] Thread %s parsed Title: -> \"%s\"%n", threadName, parsedTitle);
                return body;
            } else {
                System.err.printf("[Error] Thread %s received HTTP Code %d from %s%n", threadName, response.statusCode(), url);
            }
        } catch (IOException | InterruptedException e) {
            System.err.printf("[Exception] Thread %s failed on %s. Detail: %s%n", threadName, url, e.getMessage());
            Thread.currentThread().interrupt();
        }
        return "";
    }

    /**
     * Helper method to grab a JSON key's string value without external libraries.
     */
    private static String extractJsonField(String json, String fieldName) {
        String keyMarker = "\"" + fieldName + "\":";
        int keyIndex = json.indexOf(keyMarker);
        if (keyIndex == -1) return "Unknown";
        
        int valueStart = json.indexOf("\"", keyIndex + keyMarker.length()) + 1;
        int valueEnd = json.indexOf("\"", valueStart);
        
        if (valueStart <= 0 || valueEnd == -1) return "Unknown";
        return json.substring(valueStart, valueEnd);
    }
}
