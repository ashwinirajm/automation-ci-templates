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

        // Counters for failure types
        Map<String, Integer> failureCounts = new LinkedHashMap<>();
        failureCounts.put("TIMEOUT", 0);
        failureCounts.put("SERVER_500", 0);
        failureCounts.put("ASSERTION_FAILURE", 0);
        failureCounts.put("UNKNOWN_FAILURE", 0);

        // Store messages for each type
        Map<String, List<String>> failureMessages = new LinkedHashMap<>();
        failureMessages.put("TIMEOUT", new ArrayList<>());
        failureMessages.put("SERVER_500", new ArrayList<>());
        failureMessages.put("ASSERTION_FAILURE", new ArrayList<>());
        failureMessages.put("UNKNOWN_FAILURE", new ArrayList<>());

        BufferedReader br = new BufferedReader(new FileReader(logFile));
        String line;

        while ((line = br.readLine()) != null) {
            line = line.trim();
            String lower = line.toLowerCase();

            // Check for timeout
            if (lower.contains("timeout")) {
                failureCounts.put("TIMEOUT", failureCounts.get("TIMEOUT") + 1);
                failureMessages.get("TIMEOUT").add(line);
            }
            // Check for 500 error
            else if (lower.contains("500") || lower.contains("internal server error")) {
                failureCounts.put("SERVER_500", failureCounts.get("SERVER_500") + 1);
                failureMessages.get("SERVER_500").add(line);
            }
            // Check for assertion failures
            else if (lower.contains("assert") || lower.contains("assertionerror")) {
                failureCounts.put("ASSERTION_FAILURE", failureCounts.get("ASSERTION_FAILURE") + 1);
                failureMessages.get("ASSERTION_FAILURE").add(line);
            }
            // Any other exception line
            else if (lower.contains("exception") || lower.contains("failed")) {
                failureCounts.put("UNKNOWN_FAILURE", failureCounts.get("UNKNOWN_FAILURE") + 1);
                failureMessages.get("UNKNOWN_FAILURE").add(line);
            }
        }

        br.close();

        // Print summary
        System.out.println("\n========== FAILURE SUMMARY ==========");
        failureCounts.forEach((type, count) -> {
            System.out.printf("%-20s : %d occurrence(s)%n", type, count);
            List<String> messages = failureMessages.get(type);
            if (!messages.isEmpty()) {
                // Use LinkedHashSet to remove duplicates but preserve order
                System.out.println("  Messages:");
                messages.stream().distinct().forEach(msg -> System.out.println("    - " + msg));
            }
        });
        System.out.println("====================================\n");
    }
}
