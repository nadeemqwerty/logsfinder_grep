package com.mnq.logsfinder.service;

import com.mnq.logsfinder.dto.PaginatedResponse;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@Slf4j
@Service
public class LogSearchService {

    private final StorageService storageService;
    private static final ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    @Autowired
    public LogSearchService(StorageServiceFactory storageServiceFactory, @Value("${storage.type}") final String storageType) {
        this.storageService = storageServiceFactory.getService(storageType);
    }

    @PreDestroy
    public void destroy() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                executorService.shutdownNow();
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    log.error("Executor did not terminate");
                }
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt(); // preserve interrupt status
        }
    }

    public PaginatedResponse<String> searchLogs(final String searchKeyword, final LocalDate from, final LocalDate to, final int page, final int size, final boolean ignoreCase) {
        List<String> files = storageService.listFiles(from, to); // This should work for both S3 and local
        final String searchKeywordLowerCase = searchKeyword.toLowerCase();

        List<Future<List<String>>> futures = new ArrayList<>();
        for (String filePath : files) {
            Future<List<String>> future = executorService.submit(() -> {
                try {
                    Stream<String> lines = storageService.readFileContents(filePath);
                    if (ignoreCase) {
                        return lines.filter(line -> line.toLowerCase().contains(searchKeywordLowerCase))
                                .toList();
                    } else {
                        return lines.filter(line -> line.contains(searchKeyword))
                                .toList();
                    }
                } catch (Exception e) { // Catch a more general exception if S3 might throw something other than IOException
                    log.error("Error reading file: {}", filePath, e);
                    return new ArrayList<>();
                }
            });
            futures.add(future);
        }

        // Consolidate results and apply pagination
        List<String> result = consolidateResults(futures);
        List<String> paginatedResult = applyPagination(result, page, size);

        // Calculate total pages
        int totalPages = calculateTotalPages(result.size(), size);
        return new PaginatedResponse<>(paginatedResult, result.size(), page, size, totalPages);
    }

    private List<String> consolidateResults(List<Future<List<String>>> futures) {
        List<String> result = new ArrayList<>();
        for (Future<List<String>> future : futures) {
            try {
                result.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                log.error("Error processing future result", e);
            }
        }
        return result;
    }

    private List<String> applyPagination(List<String> results, int page, int size) {
        int skip = page * size;
        return results.stream().skip(skip).limit(size).toList();
    }

    private int calculateTotalPages(int totalItems, int size) {
        return (int) Math.ceil((double) totalItems / size);
    }

}




