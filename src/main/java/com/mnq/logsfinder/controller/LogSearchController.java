package com.mnq.logsfinder.controller;

import com.mnq.logsfinder.dto.PaginatedResponse;
import com.mnq.logsfinder.service.LogSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.Collections;

@RestController
@RequestMapping("/api/logs")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class LogSearchController {

    private final LogSearchService logSearchService;

    @GetMapping("/search")
    public ResponseEntity<?> searchLogs(@RequestParam String searchKeyword, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from, @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size, @RequestParam(defaultValue = "false") boolean ignoreCase) {

        // Validate searchKeyword is not null or empty
        if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Search keyword must not be empty.");
        }

        // Validate 'from' date is before 'to' date
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("'From' date must be before 'To' date.");
        }

        // Validate 'page' and 'size' parameters
        if (page < 0) {
            return ResponseEntity.badRequest().body("'Page' must be zero or positive.");
        }
        if (size <= 0) {
            return ResponseEntity.badRequest().body("'Size' must be positive.");
        }

        try {
            PaginatedResponse<String> response = logSearchService.searchLogs(searchKeyword, from, to, page, size, ignoreCase);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error while fetching log. msg : {} ", e.getMessage());
            return ResponseEntity.internalServerError().body(Collections.singletonMap("Error", "An internal error occurred."));
        }
    }
}
