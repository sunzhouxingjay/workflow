package com.example.serviceTest.test;

public class testException {
    public static void t() {
        try {
            throw new RuntimeException("testok");
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }
}
