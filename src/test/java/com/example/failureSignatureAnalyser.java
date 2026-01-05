package com.example;

import java.io.*;
import java.util.*;

public class failureSignatureAnalyzer {

    public static void main(String[] args) throws Exception {

        File logFile = new File("logs/test-output.log");
        if (!logFile.exists()) {
            System.out.println("❌ Log file not found: " + logFile.getAbsolutePath());
            return;
        }

        // Initialize counters for each failure type
        Map<String, Integer> failureCounts = new LinkedHashMap<>();
        failureCounts.put("TIMEOUT", 0);
        failureCounts.put("SERVER_500", 0);
        failureCounts.put("ASSERTION_FAILURE", 0);
        failureCounts.put("UNKNOWN_FAILURE", 0);

        int totalFailed = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(logFile))) {
            String line;
            while ((line = br.readLine()) != null) {
                String lowerLine = line.toLowerCase();

                // Check for known failure signatures
                if (lowerLine.contains("timeout")) {
                    failureCounts.put("TIMEOUT", failureCounts.get("TIMEOUT") + 1);
                } else if (lowerLine.contains("500")) {
                    failureCounts.put("SERVER_500", failureCounts.get("SERVER_500") + 1);
                } else if (lowerLine.contains("assert")) {
                    failureCounts.put("ASSERTION_FAILURE", failureCounts.get("ASSERTION_FAILURE") + 1);
                }

                // Count total failed tests
                if (lowerLine.contains("failed")) {
                    totalFailed++;
                }
            }
        }

        // Calculate UNKNOWN failures
        int knownCount = failureCounts.get("TIMEOUT") 
                       + failureCounts.get("SERVER_500") 
                       + failureCounts.get("ASSERTION_FAILURE");
        failureCounts.put("UNKNOWN_FAILURE", totalFailed - knownCount);

        // Print summary
        System.out.println("========== FAILURE SUMMARY ==========");
        failureCounts.forEach((key, count) -> 
            System.out.printf("%-20s : %d occurrence(s)%n", key, count)
        );
        System.out.println("====================================");
    }
}

