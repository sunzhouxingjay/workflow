package com.example.serviceTest.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class interceptorConfig implements WebMvcConfigurer {

    @Autowired
    private jwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        List<String> pathPatterns=new ArrayList<>();
        pathPatterns.add("/testHeader");
        List<String> excludePathPatterns=new ArrayList<>();
        excludePathPatterns.add("/getToken");
        registry.addInterceptor(jwtInterceptor) //添加拦截器
            .addPathPatterns(pathPatterns) //添加拦截url
            .excludePathPatterns(excludePathPatterns); //添加不拦截url
    }
}
