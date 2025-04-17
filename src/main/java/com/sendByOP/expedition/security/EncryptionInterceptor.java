package com.sendByOP.expedition.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Profile("prod")
public class EncryptionInterceptor implements HandlerInterceptor {

    private final RSAEncryptionUtil rsaEncryptionUtil;

    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (request.getContentLength() > 0) {
            String encryptedBody = new BufferedReader(new InputStreamReader(request.getInputStream()))
                    .lines().collect(Collectors.joining("\n"));
            String decryptedBody = rsaEncryptionUtil.decrypt(encryptedBody);
            request.setAttribute("decryptedBody", decryptedBody);
        }
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String responseBody = response.toString();
        if (responseBody != null && !responseBody.isEmpty()) {
            String encryptedResponse = rsaEncryptionUtil.encrypt(responseBody);
            response.setContentType("application/json");
            PrintWriter writer = response.getWriter();
            writer.write(encryptedResponse);
            writer.flush();
        }
    }
}