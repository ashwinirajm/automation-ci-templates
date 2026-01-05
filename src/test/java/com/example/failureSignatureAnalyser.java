package com.example;

import java.io.*;
import java.util.*;

public class failureSignatureAnalyser {

    // ANSI color codes
    private static final String BROWN = "\033[0;33m";    // Brown/orange for failures
    private static final String YELLOW = "\033[1;33m";   // Light yellow for TIMEOUT
    private static final String GREEN = "\033[0;32m";    // Passed
    private static final String MAGENTA = "\033[0;35m";  // Unknown failure
    private static final String CYAN = "\033[0;36m";     // Header
    private static final String NC = "\033[0m";          // No Color

    public static void main(String[] args) throws Exception {

        // Parse environment and suite from arguments
        String env = args.length > 0 ? args[0] : "UNKNOWN_ENV";
        String suite = args.length > 1 ? args[1] : "UNKNOWN_SUITE";

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
            // Passed tests (parse Maven/TestNG summary)
            else if (lower.contains("tests run") && lower.contains("failures: 0") && lower.contains("errors: 0")) {
                int run = extractTestsRun(line);
                passedCount += run;
                passedMessages.add(line.trim());
            }
        }
        br.close();

        // Print summary header
        System.out.println("\n" + CYAN + "========== TEST SUMMARY [" + env.toUpperCase() + " | " + suite.toUpperCase() + "] ==========" + NC);

        // Print failures
        failureCounts.forEach((type, count) -> {
            String color = switch (type) {
                case "TIMEOUT" -> YELLOW;
                case "SERVER_500", "ASSERTION_FAILURE" -> BROWN;
                case "UNKNOWN_FAILURE" -> MAGENTA;
                default -> NC;
            };
            System.out.printf(color + "%-20s : %d occurrence(s)%s%n", type, count, NC);
            List<String> messages = failureMessages.get(type);
            if (!messages.isEmpty()) {
                messages.stream().distinct().forEach(msg -> System.out.println("    - " + msg));
            }
        });

        // Print passed tests
        if (passedCount > 0) {
            System.out.printf(GREEN + "PASSED              : %d occurrence(s)%s%n", passedCount, NC);
            passedMessages.stream().distinct().forEach(msg -> System.out.println(GREEN + "    - " + msg + NC));
        }

        // Footer
        System.out.println(CYAN + "====================================" + NC + "\n");
    }

    /**
     * Extracts the number of tests run from a Maven/TestNG summary line.
     * Example line: [INFO] Tests run: 3, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.123 s
     */
    private static int extractTestsRun(String line) {
        try {
            int start = line.toLowerCase().indexOf("tests run:");
            if (start < 0) return 0;
            String sub = line.substring(start + 10);
            String[] parts = sub.split(",");
            return Integer.parseInt(parts[0].trim());
        } catch (Exception e) {
            return 0;
        }
    }
}
