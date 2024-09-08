package com.verve.verveChallenge.controller;

import com.verve.verveChallenge.exception.HttpRequestException;
import com.verve.verveChallenge.service.UniqueRequestCounterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@EnableScheduling
@RestController
@RequestMapping("/api/verve")
public class UniqueRequestCounterController {
    private static final Logger logger = LoggerFactory.getLogger(UniqueRequestCounterController.class);

    @Autowired
    private UniqueRequestCounterService uniqueRequestCounterService;

    @GetMapping("accept")
    public ResponseEntity<String> acceptRequest(@RequestParam int id, @RequestParam(required = false) String endpoint) {
        try {
            uniqueRequestCounterService.processRequests(id, endpoint);
            return ResponseEntity.status(HttpStatus.OK).body("ok");
        } catch (HttpRequestException hse) {
            logger.error("Error sending HTTP GET request to {}. Status code: {}", endpoint, hse.getStatusCode());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
        catch (Exception e) {
            logger.error("Filed to send http request: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed");
        }
    }
}
