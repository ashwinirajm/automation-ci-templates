package com.example;

import org.testng.annotations.Test;
import java.util.Properties;
import java.io.FileInputStream;

public class SampleTest {

    @Test
    public void testEnvironment() throws Exception {
        String env = System.getProperty("env", "qa");
        Properties props = new Properties();
        props.load(new FileInputStream("src/test/resources/" + env + ".properties"));
        System.out.println("Running test in environment: " + env);
        System.out.println("Base URL: " + props.getProperty("base.url"));
    }
}

