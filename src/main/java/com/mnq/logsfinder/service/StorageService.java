package com.mnq.logsfinder.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

public interface StorageService {

    List<String> listFiles(LocalDate from, LocalDate to);

    List<String> listFiles(String directoryPath, LocalDate from, LocalDate to);

    Stream<String> readFileContents(String filePath) throws Exception;
}
