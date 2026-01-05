package com.example;

import org.testng.annotations.Test;
import java.util.Properties;
import java.io.FileInputStream;
import org.testng.Assert;

public class sampleTest {
    
    @Test
    public void sampleTest() {
        String env = System.getProperty("env", "qa");
        String suite = System.getProperty("suite", "smoke");

        System.out.println("=================================");
        System.out.println("Executing Suite : " + suite);
        System.out.println("Executing Env   : " + env);
        System.out.println("=================================");

        // Intentional failure for demo
        Assert.assertEquals(1, 2, "Assertion failure demo");
    }
}

