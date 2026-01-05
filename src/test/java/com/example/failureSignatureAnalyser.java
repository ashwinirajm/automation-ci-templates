package com.example;

import java.io.*;
import java.util.*;

public class failureSignatureAnalyser {

    // ANSI color codes for console
    private static final String RED = "\033[0;31m";
    private static final String GREEN = "\033[0;32m";
    private static final String YELLOW = "\033[0;33m";
    private static final String NC = "\033[0m"; // No Color

    public static void main(String[] args) throws Exception {
        File logFile = new File("logs/test-output.log");
        if (!logFile.exists()) {
            System.out.println("❌ Log file not found: " + logFile.getAbsolutePath());
            return;
        }

        // Counters for failure types
        Map<String, Integer> failureCounts = new LinkedHashMap<>();
        failureCounts.put("TIMEOUT", 0);
        failureCounts.put("SERVER_500", 0);
        failureCounts.put("ASSERTION_FAILURE", 0);
        failureCounts.put("UNKNOWN_FAILURE", 0);
        int passedCount = 0;

        // Messages for each type
        Map<String, List<String>> failureMessages = new LinkedHashMap<>();
        failureMessages.put("TIMEOUT", new ArrayList<>());
        failureMessages.put("SERVER_500", new ArrayList<>());
        failureMessages.put("ASSERTION_FAILURE", new ArrayList<>());
        failureMessages.put("UNKNOWN_FAILURE", new ArrayList<>());
        List<String> passedMessages = new ArrayList<>();

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String line;
        while ((line = br.readLine()) != null) {
            String lower = line.toLowerCase();

            // Failures
            if (lower.contains("timeout")) {
                failureCounts.put("TIMEOUT", failureCounts.get("TIMEOUT") + 1);
                failureMessages.get("TIMEOUT").add(line);
            } else if (lower.contains("500") || lower.contains("internal server error")) {
                failureCounts.put("SERVER_500", failureCounts.get("SERVER_500") + 1);
                failureMessages.get("SERVER_500").add(line);
            } else if (lower.contains("assert") || lower.contains("assertionerror")) {
                failureCounts.put("ASSERTION_FAILURE", failureCounts.get("ASSERTION_FAILURE") + 1);
                failureMessages.get("ASSERTION_FAILURE").add(line);
            } else if (lower.contains("exception") || lower.contains("failed")) {
                failureCounts.put("UNKNOWN_FAILURE", failureCounts.get("UNKNOWN_FAILURE") + 1);
                failureMessages.get("UNKNOWN_FAILURE").add(line);
            } 
            // Passed tests (Maven/TestNG lines)
            else if (lower.contains("tests run") && lower.contains("success")) {
                passedCount++;
                passedMessages.add(line);
            }
        }
        br.close();

        // Print summary with colors
        System.out.println("\n========== FAILURE SUMMARY ==========");

        // Failures
        failureCounts.forEach((type, count) -> {
            String color = (type.equals("TIMEOUT")) ? YELLOW : RED;
            System.out.printf(color + "%-20s : %d occurrence(s)%s%n", type, count, NC);
            List<String> messages = failureMessages.get(type);
            if (!messages.isEmpty()) {
                System.out.println("  Messages:");
                messages.stream().distinct().forEach(msg -> System.out.println("    - " + msg));
            }
        });

        // Passed tests
        if (passedCount > 0) {
            System.out.printf(GREEN + "PASSED              : %d occurrence(s)%s%n", passedCount, NC);
            passedMessages.stream().distinct().forEach(msg -> System.out.println(GREEN + "    - " + msg + NC));
        }

        System.out.println("====================================\n");
    }
}
