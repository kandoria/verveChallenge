package com.verve.verveChallenge.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.*;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import com.verve.verveChallenge.exception.HttpRequestException;

@Service
public class UniqueRequestCounterService {
    private static final Logger consoleLogger = LoggerFactory.getLogger(UniqueRequestCounterService.class);
    private static final Logger fileLogger = LoggerFactory.getLogger("FileLogger");
    private static final String kafkaTopic = "unique-request-counts";

    // Created for Part 1 implementation
    // private final Map<String, Set<Integer>> minuteRequests = new ConcurrentHashMap<>();

    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;
    @Autowired
    private RestTemplate restTemplate;

    public void processRequests(int id, String endPoint) throws Exception {
        String currentMinute = getCurrentMinute();
        // Created for Part 1 implementation
        // minuteRequests.computeIfAbsent(currentMinute, k -> ConcurrentHashMap.newKeySet()).add(id);
        Long result = redisTemplate.opsForSet().add(currentMinute, String.valueOf(id));
        boolean isUnique = Objects.equals(result, 1L);

        if(isUnique && endPoint!=null && !endPoint.isEmpty()) {
            int uniqueCount = getUniqueCount(currentMinute);
            ResponseEntity<String> response = sendHttpPostRequest(endPoint, uniqueCount);
            consoleLogger.info("HTTP POST request sent to {}. Status code: {}", endPoint, response.getStatusCode());
        }
    }

    // Scheduled to get unique counts in last minute.
    @Scheduled(fixedRate = 60000)
    public void logUniqueRequests() {
        String previousMinute = getPreviousMinute();
        int uniqueCount = getUniqueCount(previousMinute);
        // Part 1 implementation
        fileLogger.info("Unique requests in the last minute: {}", uniqueCount);
        sentToKafka(uniqueCount);
        redisTemplate.delete(previousMinute);
    }

    private String getCurrentMinute() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }

    private String getPreviousMinute() {
        return String.valueOf((System.currentTimeMillis() / 60000)-1);
    }

    private int getUniqueCount(String minute) {
        Long size = redisTemplate.opsForSet().size(minute);
        return size != null ? size.intValue() : 0;
    }

    private void sentToKafka(int uniqueCount) {
        String message = String.format("Unique requests in the last minute: %d", uniqueCount);
        kafkaTemplate.send(kafkaTopic, message);
        consoleLogger.info("Sent to Kafka: {}", message);
    }

    // Created for Part 1 implementation
    private void sendHttpGetRequest(String endPoint, int count) throws Exception {
        try {
            String url = String.format("%s?count=%d", endPoint, count);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            consoleLogger.info("HTTP GET request sent to {}. Status code: {}", url, response.getStatusCode());
        } catch (HttpStatusCodeException hse) {
            throw new HttpRequestException("HTTP request failed", hse.getStatusCode());
        }
        catch (Exception e) {
            throw new Exception("Failed to process http request: " + e.getMessage());
        }
    }

    private ResponseEntity<String> sendHttpPostRequest(String endPoint, int count) throws Exception {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("uniqueCount", count);
            requestBody.put("timestamp", LocalDateTime.now().toString());

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

            return restTemplate.postForEntity(endPoint, request, String.class);
        } catch (HttpStatusCodeException hse) {
            throw new HttpRequestException("HTTP request failed", hse.getStatusCode());
        }
        catch (Exception e) {
            throw new Exception("HTTP POST request failed");
        }
    }
}
