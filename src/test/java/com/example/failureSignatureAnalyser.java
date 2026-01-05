package com.example;

import java.io.*;
import java.util.*;

public class failureSignatureAnalyser {

    public static void main(String[] args) throws Exception {
        File logFile = new File("logs/test-output.log");
        if (!logFile.exists()) {
            System.out.println("❌ Log file not found: " + logFile.getAbsolutePath());
            return;
        }

        // Counters
        Map<String, Integer> failureCounts = new LinkedHashMap<>();
        failureCounts.put("TIMEOUT", 0);
        failureCounts.put("SERVER_500", 0);
        failureCounts.put("ASSERTION_FAILURE", 0);
        failureCounts.put("UNKNOWN_FAILURE", 0);

        // Map to track which test failed in which category
        Map<String, List<String>> failureTests = new LinkedHashMap<>();
        failureTests.put("TIMEOUT", new ArrayList<>());
        failureTests.put("SERVER_500", new ArrayList<>());
        failureTests.put("ASSERTION_FAILURE", new ArrayList<>());
        failureTests.put("UNKNOWN_FAILURE", new ArrayList<>());

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String line;
        String currentTest = null;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            String lower = line.toLowerCase();

            // Detect test name lines (from TestNG log)
            if (line.startsWith("Running ")) {
                currentTest = line.substring("Running ".length()).trim();
            }

            // Categorize failure
            if (lower.contains("timeout")) {
                failureCounts.put("TIMEOUT", failureCounts.get("TIMEOUT") + 1);
                if (currentTest != null) failureTests.get("TIMEOUT").add(currentTest);
            } else if (lower.contains("500") || lower.contains("internal server error")) {
                failureCounts.put("SERVER_500", failureCounts.get("SERVER_500") + 1);
                if (currentTest != null) failureTests.get("SERVER_500").add(currentTest);
            } else if (lower.contains("assert") || lower.contains("assertionerror")) {
                failureCounts.put("ASSERTION_FAILURE", failureCounts.get("ASSERTION_FAILURE") + 1);
                if (currentTest != null) failureTests.get("ASSERTION_FAILURE").add(currentTest);
            } else if (lower.contains("exception") || lower.contains("failed")) {
                failureCounts.put("UNKNOWN_FAILURE", failureCounts.get("UNKNOWN_FAILURE") + 1);
                if (currentTest != null) failureTests.get("UNKNOWN_FAILURE").add(currentTest);
            }
        }

        br.close();

        // Print summary with occurrences and tests
        System.out.println("========== FAILURE SUMMARY ==========");
        failureCounts.forEach((key, count) -> {
            System.out.printf("%-20s : %d occurrence(s)%n", key, count);
            List<String> tests = failureTests.get(key);
            if (!tests.isEmpty()) {
                System.out.println("  Tests: " + String.join(", ", new LinkedHashSet<>(tests)));
            }
        });
        System.out.println("====================================");
    }
}
