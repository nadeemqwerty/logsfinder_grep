package com.mnq.logsfinder.service.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ListObjectsV2Request;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.mnq.logsfinder.service.StorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Service
public class S3StorageService implements StorageService {

    private final AmazonS3 amazonS3;
    private final String bucketName;
    private final String basePath;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    public S3StorageService(AmazonS3 amazonS3, @Value("${aws.s3.bucket.name}") String bucketName, @Value("${aws.s3.base.path}") String basePath) {
        this.amazonS3 = amazonS3;
        this.bucketName = bucketName;
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
    }

    @Override
    public List<String> listFiles(LocalDate from, LocalDate to) {
        return listFiles(basePath, from, to); // Utilize the base path for listing
    }

    @Override
    public List<String> listFiles(String directoryPath, LocalDate from, LocalDate to) {
        List<String> fileNames = new ArrayList<>();

        while (!from.isAfter(to)) {
            String dailyPrefix = directoryPath + from.format(formatter) + "/";
            ListObjectsV2Result result = amazonS3.listObjectsV2(new ListObjectsV2Request()
                    .withBucketName(bucketName)
                    .withPrefix(dailyPrefix));

            for (S3ObjectSummary summary : result.getObjectSummaries()) {
                fileNames.add(summary.getKey());
            }
            from = from.plusDays(1);
        }

        return fileNames;
    }

    @Override
    public Stream<String> readFileContents(String filePath) throws Exception {
        String fullPath = filePath.startsWith(basePath) ? filePath : basePath + filePath;
        BufferedReader reader = new BufferedReader(new InputStreamReader(amazonS3.getObject(bucketName, fullPath).getObjectContent(), StandardCharsets.UTF_8));
        return reader.lines();
    }
}
