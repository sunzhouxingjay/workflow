package com.example.serviceTest.interceptor;

import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@Component
@CrossOrigin
public class jwtInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equals(method)){
            return true;
        }
        //获取请求头部信息
        String token = request.getHeader("token");
        if (token!=null) {
            try {
                JwtParser jwtParser = Jwts.parser();
                jwtParser.setSigningKey("i am dayuanzhong");
                //只做token验证，若不通过，则抛异常
                Jws<Claims> claimsJws = jwtParser.parseClaimsJws(token);
                return true;
            }catch (ExpiredJwtException e){
                doResponse(response,"Token已过期，请重新登陆！");
                return false;
            }catch (UnsupportedJwtException e){
                doResponse(response,"Token不合法，请自重！");
                return false;
            }catch (Exception e){
                doResponse(response,"Token不合法，请自重！");
                return false;
            }
        }
        doResponse(response,"Token已过期，请重新登陆！");
        return false;
    }

    public void doResponse(HttpServletResponse response,String info) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        PrintWriter writer = response.getWriter();
        ResponseEntity<String> responseEntity=new ResponseEntity<>(info, HttpStatus.NOT_ACCEPTABLE);
        String json = JSON.toJSONString(responseEntity);
        writer.write(json);
        writer.flush();
        writer.close();
    }
}
