package org.example.matket.infra.naverBand.controller;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matket.infra.naverBand.dto.BandTokenResponse;
import org.example.matket.infra.naverBand.service.BandAuthService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Slf4j
@RestController
@RequestMapping("/band")
@RequiredArgsConstructor
public class BandAuthController {

    private final BandAuthService bandAuthService;

    // 1. 사용자가 이 주소로 들어오면 밴드 로그인 페이지로 보냄
    // http://localhost:8080/band/login
    @GetMapping("/login")
    public void login(HttpServletResponse response) throws IOException {
        String loginUrl = bandAuthService.getLoginUrl();
        log.info("Redirecting to Band Login: {}", loginUrl);
        response.sendRedirect(loginUrl);
    }

    // 2. 밴드 로그인이 끝나면 밴드가 이 주소를 호출해줌 (Callback)
    // http://localhost:8080/band/callback
    @GetMapping("/callback")
    public String callback(@RequestParam String code) {
        log.info("Received Authorization Code: {}", code);

        // 서비스에게 코드를 주고 토큰을 받아오라고 시킴
        BandTokenResponse tokenResponse = bandAuthService.getAccessToken(code);

        log.info("Access Token Issued: {}", tokenResponse.getAccessToken());

        // 눈으로 확인하기 위해 화면에 토큰 정보를 뿌려줌
        return "토큰 발급 성공! <br/> Access Token: " + tokenResponse.getAccessToken();
    }
}