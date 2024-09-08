package com.verve.verveChallenge.service;

import com.verve.verveChallenge.controller.UniqueRequestCounterController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import com.verve.verveChallenge.exception.HttpRequestException;

@Service
public class UniqueRequestCounterService {
    private static final Logger logger = LoggerFactory.getLogger(UniqueRequestCounterController.class);
    private static final Logger fileLogger = LoggerFactory.getLogger("FileLogger");
    private final Map<String, Set<Integer>> minuteRequests = new ConcurrentHashMap<>();

    @Autowired
    private RestTemplate restTemplate;

    public void processRequests(int id, String endPoint) throws Exception {
        String currentMinute = getCurrentMinute();
        minuteRequests.computeIfAbsent(currentMinute, k -> ConcurrentHashMap.newKeySet()).add(id);

        if(endPoint!=null && !endPoint.isEmpty()) {
            int uniqueCount = getUniqueCount(currentMinute);
            sendHttpGetRequest(endPoint, uniqueCount);
        }
    }

    @Scheduled(fixedRate = 60000)
    public void logUniqueRequests() {
        String previousMinute = getPreviousMinute();
        int uniqueCount = getUniqueCount(previousMinute);
        fileLogger.info("Unique requests in the last minute: {}", uniqueCount);
        minuteRequests.remove(previousMinute);
    }

    private String getCurrentMinute() {
        return String.valueOf(System.currentTimeMillis() / 60000);
    }

    private String getPreviousMinute() {
        return String.valueOf((System.currentTimeMillis() / 60000)-1);
    }

    private int getUniqueCount(String minute) {
        Set<Integer> requests = minuteRequests.get(minute);
        return requests!=null ? requests.size() : 0;
    }

    private void sendHttpGetRequest(String endPoint, int count) throws Exception {
        try {
            String url = String.format("%s?count=%d", endPoint, count);
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            logger.info("HTTP GET request sent to {}. Status code: {}", url, response.getStatusCode());
        } catch (HttpStatusCodeException hse) {
            throw new HttpRequestException("HTTP request failed", hse.getStatusCode());
        }
        catch (Exception e) {
            throw new Exception("Failed to process http request: " + e.getMessage());
        }
    }
}
