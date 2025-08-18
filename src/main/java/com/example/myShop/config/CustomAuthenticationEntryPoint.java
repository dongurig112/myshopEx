package com.example.myShop.config;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

//인증되지 않은 사용자가 보호된 리소스에 접근하려 할 때 처리하는 진입점 클래스
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    //인증이 필요한 요청이 들어왔지만 인증되지 않았을 경우 실행되는 메서드
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
    }
}
