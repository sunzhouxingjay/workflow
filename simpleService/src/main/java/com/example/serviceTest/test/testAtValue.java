package com.example.serviceTest.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class testAtValue {
    private static String home;
    @Value("${test}")
    private String h;

    public String getHome() {
        return home;
    }

    

    @Value(value ="${test}")
    public void setHome(String home) {
        System.out.println("我在设置："+home);
        testAtValue.home = home;
    }



    public String getH() {
        return h;
    }
    
}
