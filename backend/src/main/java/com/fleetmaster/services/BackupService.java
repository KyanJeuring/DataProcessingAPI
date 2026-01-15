package com.fleetmaster.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
public class BackupService {

    @Value("${spring.datasource.url}")
    private String dbUrl;

    @Value("${spring.datasource.username}")
    private String dbUser;

    @Value("${spring.datasource.password}")
    private String dbPassword;

    // Run every day at 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    public void performBackup() {
        System.out.println("Starting automated database backup...");
        try {
            // We assume the host is 'postgres' as per docker-compose
            String host = "postgres";
            String dbName = "dataprocessingapi_db";
            
            // Try to parse from URL if possible, e.g. "jdbc:postgresql://postgres:5432/dataprocessingapi_db"
            if (dbUrl != null && dbUrl.contains("://")) {
                String cleanUrl = dbUrl.substring(dbUrl.indexOf("://") + 3);
                String[] parts = cleanUrl.split("/");
                if (parts.length >= 2) {
                    host = parts[0].split(":")[0];
                    dbName = parts[1].split("\\?")[0];
                }
            }

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String fileName = "backup_" + timestamp + ".sql";
            // Storing in a persisted volume would be better, but /tmp is fine for demo
            File backupFile = new File("/tmp/" + fileName);

            ProcessBuilder pb = new ProcessBuilder(
                "pg_dump",
                "-h", host,
                "-U", dbUser,
                "--no-password", 
                "-f", backupFile.getAbsolutePath(),
                dbName
            );
            
            // Pass password securely via environment variable
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);

            Process process = pb.start();
            boolean finished = process.waitFor(60, TimeUnit.SECONDS);

            if (finished && process.exitValue() == 0) {
                System.out.println("Backup successful: " + backupFile.getAbsolutePath());
            } else {
                System.err.println("Backup failed. Exit code: " + (finished ? process.exitValue() : "timeout"));
                try (var reader = new java.io.BufferedReader(new java.io.InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.err.println(line);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Backup exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
