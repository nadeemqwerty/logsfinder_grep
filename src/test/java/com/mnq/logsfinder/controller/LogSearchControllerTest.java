package com.mnq.logsfinder.controller;

import com.mnq.logsfinder.dto.PaginatedResponse;
import com.mnq.logsfinder.service.LogSearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.util.Collections;

@WebMvcTest(LogSearchController.class)
class LogSearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LogSearchService logSearchService;

    @Test
    void shouldReturnPaginatedLogs() throws Exception {
        // Given
        String searchKeyword = "error";
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 1, 2);
        int page = 0;
        int size = 10;
        boolean ignoreCase = true;

        PaginatedResponse<String> expectedResponse = new PaginatedResponse<>(
                Collections.singletonList("log content"), // Mock log content
                1, // Total items
                page, // Current page
                size, // Items per page
                1 // Total pages
        );

        given(logSearchService.searchLogs(
                searchKeyword, from, to, page, size, ignoreCase))
                .willReturn(expectedResponse);

        // When & Then
        mockMvc.perform(get("/api/logs/search")
                        .param("searchKeyword", searchKeyword)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .param("page", String.valueOf(page))
                        .param("size", String.valueOf(size))
                        .param("ignoreCase", String.valueOf(ignoreCase))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{ /* Expected JSON response */ }")); // Replace with the expected JSON
    }

    @Test
    void shouldReturnBadRequestForMissingKeyword() throws Exception {
        // Given
        String searchKeyword = "";
        LocalDate from = LocalDate.of(2023, 1, 1);
        LocalDate to = LocalDate.of(2023, 1, 2);

        // When & Then
        mockMvc.perform(get("/api/logs/search")
                        .param("searchKeyword", searchKeyword)
                        .param("from", from.toString())
                        .param("to", to.toString())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
}
