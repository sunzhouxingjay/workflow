package com.example.serviceTest.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


import com.example.serviceTest.exception.allocateTableException;
import com.example.serviceTest.helper.jsonTransfer;
import com.example.serviceTest.test.testAtValue;

import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;


@Controller
public class ServiceController {
    private static Map<String,Object> testGradeTable=new HashMap<String,Object>() {{
        put("xiaoming",80);
        put("小红",99);
        put("xiaoxiao",75);
    }};
    private Logger logger=LoggerFactory.getLogger(ServiceController.class);
    @Autowired
    testAtValue testAtValue;

    @RequestMapping(value= "/getToken", method = RequestMethod.GET)
    @ResponseBody
    public String getToken() {
        JwtBuilder jwtBuilder=Jwts.builder();
        Map<String,Object> map=new HashMap<String,Object>(){{
            put("key","testUser");
        }};
        String token=jwtBuilder.setSubject("tokenTest")
                               .setIssuedAt(new Date())
                               .setId("1")
                               .setClaims(map)
                               .setExpiration(new Date(System.currentTimeMillis()+3600*1000L))
                               .signWith(SignatureAlgorithm.HS256, "i am dayuanzhong")
                               .compact();
        return jsonTransfer.mapToJsonString(new HashMap<String,Object>(){{put("token",token);}});
    }

    @RequestMapping(value="/testAtValue",method=RequestMethod.GET)
    @ResponseBody
    public String testAtValue() {
        return testAtValue.getHome();
    }

    @RequestMapping(value="/testHeader",method={RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String testHeader() {
        Map<String,Object> res=new HashMap<String,Object>(){{
            put("result","test ok");
        }};
        System.out.println(res);
        return jsonTransfer.mapToJsonString(res);
    }

    @RequestMapping(value="/formTest",method=RequestMethod.GET)
    @ResponseBody
    public String formTest(@RequestParam("username") String username) {
        System.out.println(username);
        return "ok";
    }

    @RequestMapping(value="/testForward",method = RequestMethod.GET)
    @ResponseBody
    public void testForward(HttpServletRequest req,HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/hello").forward(req, resp);
    }

    @RequestMapping(value = "/hello", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String hello() {
        logger.info("get hello ok");
        return "hello";
    }
    @RequestMapping(value = "/test", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String test(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        for (String key:requestMap.keySet()) {
            System.out.println(key+":"+String.valueOf(requestMap.get(key)));
        }
        return req;
    }

    @ExceptionHandler(value = RuntimeException.class) 
    public ResponseEntity<String> defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        //Map<String,Object> map=new HashMap<>();
        return new ResponseEntity<String>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
    
    @RequestMapping(value="/testException")
    @ResponseBody
    public String testException() {
        com.example.serviceTest.test.testException.t();
        return "OK";
    }

    @RequestMapping(value = "/query", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String query(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String name=String.valueOf(requestMap.get("name"));
        Map<String,Object> map=new HashMap<>();
        if (testGradeTable.get(name)==null) {
            map.put(name,"没有");
        } else {
            map.put(name,testGradeTable.get(name));
        }
        return jsonTransfer.mapToJsonString(map);
    }

    @RequestMapping(value = "/add", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String add(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> requestMap=jsonTransfer.jsonToMap(req);
        String name=String.valueOf(requestMap.get("name"));
        String grade=String.valueOf(requestMap.get("grade"));
        testGradeTable.put(name,grade);
        return "add success";
    }

    @RequestMapping(value = "/service1", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String service1(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> responseMap=new HashMap<>();
        responseMap.put("code",200);
        responseMap.put("body","get service1 success");
        return jsonTransfer.mapToJsonString(responseMap);
    }

    @RequestMapping(value = "/service2", method = {RequestMethod.GET,RequestMethod.POST})
    @ResponseBody
    public String service2(@RequestBody String req) throws IOException, InterruptedException, ExecutionException {
        Map<String,Object> responseMap=new HashMap<>();
        responseMap.put("code",200);
        responseMap.put("body","get service2 success");
        return jsonTransfer.mapToJsonString(responseMap);
    }
}
