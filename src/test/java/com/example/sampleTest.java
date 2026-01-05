package com.example;

import org.testng.Assert;
import org.testng.annotations.Test;

public class sampleTest {

    @Test
    public void printSuiteAndEnv() {
        String env = System.getProperty("env", "qa");
        String suite = System.getProperty("suite", "smoke");

        System.out.println("=================================");
        System.out.println("Executing Suite : " + suite);
        System.out.println("Executing Env   : " + env);
        System.out.println("=================================");
    }

    @Test
    public void timeoutTest1() throws InterruptedException {
        System.out.println("Simulating Timeout 1...");
        Thread.sleep(500);
        throw new RuntimeException("Timeout occurred in test 1");
    }

    @Test
    public void timeoutTest2() throws InterruptedException {
        System.out.println("Simulating Timeout 2...");
        Thread.sleep(500);
        throw new RuntimeException("Timeout occurred in test 2");
    }

    @Test
    public void server500Test() {
        System.out.println("Simulating HTTP 500...");
        throw new RuntimeException("HTTP 500 Internal Server Error");
    }

    @Test
    public void assertionFailureTest() {
        System.out.println("Simulating Assertion Failure...");
        Assert.assertEquals(1, 2, "Intentional assertion failure");
    }

    @Test
    public void passedTest() {
        System.out.println("This test passes successfully.");
        Assert.assertTrue(true);
    }
}
