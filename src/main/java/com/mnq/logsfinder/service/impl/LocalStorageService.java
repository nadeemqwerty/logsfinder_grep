package com.mnq.logsfinder.service.impl;

import com.mnq.logsfinder.service.StorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
public class LocalStorageService implements StorageService {

    private static final Logger logger = LoggerFactory.getLogger(LocalStorageService.class);
    private static final DateTimeFormatter dirFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final Path baseDir;

    @Autowired
    public LocalStorageService(@Value("${local.storage.baseDir}") final String baseDir) {
        this.baseDir = Paths.get(baseDir).toAbsolutePath();
    }

    @Override
    public List<String> listFiles(final LocalDate from, final LocalDate to) {
        return listFiles(this.baseDir.toString(), from, to);
    }


    @Override
    public List<String> listFiles(final String baseDirectoryPath, final LocalDate fromDate, final LocalDate toDate) {
        List<String> filesList = new ArrayList<>();

        try (Stream<Path> walk = Files.walk(baseDir, 1)) {
            walk.filter(Files::isDirectory).forEach(dir -> {
                String dirName = dir.getFileName().toString();
                LocalDate dirDate;
                try {
                    dirDate = LocalDate.parse(dirName, dirFormatter);
                } catch (Exception e) {
                    // This directory name is not a date, skip it
                    return;
                }
                if ((dirDate.isAfter(fromDate) || dirDate.isEqual(fromDate)) && (dirDate.isBefore(toDate) || dirDate.isEqual(toDate))) {
                    try (Stream<Path> filesInDir = Files.list(dir)) {
                        filesInDir.filter(file -> file.toString().endsWith(".txt")).forEach(file -> filesList.add(file.toString()));
                    } catch (IOException e) {
                        logger.error("Error listing files in directory {}", dir, e);
                    }
                }
            });
        } catch (IOException e) {
            logger.error("Error walking through base directory {}", baseDirectoryPath, e);
        }
        return filesList;
    }

    @Override
    public Stream<String> readFileContents(final String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.lines(path);
    }
}
