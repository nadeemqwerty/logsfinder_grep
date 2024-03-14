package com.mnq.logsfinder.service;

import com.mnq.logsfinder.dto.PaginatedResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LogSearchServiceTest {


    @Mock
    private StorageServiceFactory storageServiceFactory;
    @Mock
    private StorageService storageService;
    private LogSearchService logSearchService;

    @BeforeEach
    void setUp() {
        when(storageServiceFactory.getService(Mockito.any())).thenReturn(storageService);
        logSearchService = new LogSearchService(storageServiceFactory, "s3");
    }

    @Test
    void searchLogsIgnoreCaseTrue() throws Exception {
        String content = "Error found in application";
        Stream<String> fileStream = Stream.of(content);
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log.txt"));
        when(storageService.readFileContents("log.txt")).thenReturn(fileStream);

        PaginatedResponse<String> response = logSearchService.searchLogs("error", from, to, 0, 10, true);

        assertNotNull(response);
        assertFalse(response.getItems().isEmpty());
        assertTrue(response.getItems().contains(content));
    }

    @Test
    void searchLogsIgnoreCaseFalse() throws Exception {
        String content = "Error found in application";
        Stream<String> fileStream = Stream.of(content);
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log.txt"));
        when(storageService.readFileContents("log.txt")).thenReturn(fileStream);

        PaginatedResponse<String> response = logSearchService.searchLogs("Error", from, to, 0, 10, false);

        assertNotNull(response);
        assertFalse(response.getItems().isEmpty());
        assertTrue(response.getItems().contains(content));
    }


    @Test
    void searchLogsWhenNoFilesFound() throws Exception {
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of());

        PaginatedResponse<String> response = logSearchService.searchLogs("error", from, to, 0, 10, true);

        assertTrue(response.getItems().isEmpty());
        assertEquals(0, response.getTotalPages());
    }

    @Test
    void searchLogsWithEmptyContentLines() throws Exception {
        Stream<String> fileStream = Stream.of("");
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log.txt"));
        when(storageService.readFileContents("log.txt")).thenReturn(fileStream);

        PaginatedResponse<String> response = logSearchService.searchLogs("error", from, to, 0, 10, true);

        assertTrue(response.getItems().isEmpty());
    }


    @Test
    void searchLogsWhenKeywordNotFound() throws Exception {
        Stream<String> fileStream = Stream.of("No error here");
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log.txt"));
        when(storageService.readFileContents("log.txt")).thenReturn(fileStream);

        PaginatedResponse<String> response = logSearchService.searchLogs("Exception", from, to, 0, 10, true);

        assertTrue(response.getItems().isEmpty());
    }

    @Test
    void searchLogsWhenPageBeyondTotal() throws Exception {
        Stream<String> fileStream = Stream.of("Error found here");
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log.txt"));
        when(storageService.readFileContents("log.txt")).thenReturn(fileStream);

        // Assuming that the size is 1, but we request page 2 (beyond total pages)
        PaginatedResponse<String> response = logSearchService.searchLogs("error", from, to, 2, 1, true);

        assertTrue(response.getItems().isEmpty());
        assertEquals(1, response.getTotalPages()); // Assuming only one record matched, hence one page
    }

    @Test
    void searchLogsValidPagination() throws Exception {
        // Assuming that each file contains a unique line that matches the search keyword
        Stream<String> fileStream1 = Stream.of("Error in application log 1");
        Stream<String> fileStream2 = Stream.of("Error in application log 2");
        LocalDate from = LocalDate.now();
        LocalDate to = LocalDate.now().plusDays(1);

        when(storageService.listFiles(from, to)).thenReturn(List.of("log1.txt", "log2.txt"));
        when(storageService.readFileContents("log1.txt")).thenReturn(fileStream1);
        when(storageService.readFileContents("log2.txt")).thenReturn(fileStream2);

        // Request the first page with a size of 1, expecting only the first result
        PaginatedResponse<String> response = logSearchService.searchLogs("error", from, to, 0, 1, true);

        assertEquals(1, response.getItems().size());
        assertEquals("Error in application log 1", response.getItems().get(0));
        assertEquals(2, response.getTotalItems());
        assertEquals(2, response.getTotalPages());
    }

}
