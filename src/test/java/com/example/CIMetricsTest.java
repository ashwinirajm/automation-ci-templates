package com.example;

import org.testng.Assert;
import org.testng.annotations.Test;
import org.testng.SkipException;

public class CIMetricsTest {

    @Test
    public void passedTest1() {
        System.out.println("This test passes successfully.");
        Assert.assertTrue(true);
    }

    @Test
    public void passedTest2() {
        System.out.println("This test also passes successfully.");
        Assert.assertEquals(2, 2);
    }

    @Test
    public void failedTest1() {
        System.out.println("This test is intentionally failing.");
        Assert.assertEquals(1, 2);
    }

    @Test
    public void failedTest2() {
        System.out.println("This test is intentionally failing.");
        throw new RuntimeException("Intentional failure for CI metrics");
    }

    @Test
    public void skippedTest() {
        System.out.println("This test will be skipped.");
        throw new SkipException("Skipping this test intentionally");
    }

    @Test
    public void passedTest3() {
        System.out.println("Another passing test.");
        Assert.assertTrue(true);
    }
}
