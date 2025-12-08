package org.example.matket.infra.naverBand.service;

import org.example.matket.infra.naverBand.dto.BandTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;

@Service
public class BandAuthService {

    @Value("${band.client-id}")
    private String clientId;

    @Value("${band.client-secret}")
    private String clientSecret;

    @Value("${band.redirect-uri}")
    private String redirectUri;

    private final String AUTH_URL = "https://auth.band.us/oauth2/authorize";
    private final String TOKEN_URL = "https://auth.band.us/oauth2/token";

    // 1단계: 로그인 페이지 URL 생성
    public String getLoginUrl() {
        return AUTH_URL + "?response_type=code" +
                "&client_id=" + clientId +
                "&redirect_uri=" + redirectUri;
    }

    // 2단계: 코드를 받아서 토큰으로 교환
    public BandTokenResponse getAccessToken(String code) {
        RestTemplate restTemplate = new RestTemplate();

        // 헤더 설정 (Basic Auth: ClientID와 Secret을 Base64로 인코딩)
        HttpHeaders headers = new HttpHeaders();
        String authValue = clientId + ":" + clientSecret;
        String encodedAuth = Base64.getEncoder().encodeToString(authValue.getBytes());
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.set("Content-Type", "application/x-www-form-urlencoded"); // 문서에 명시된 필수 사항은 아니지만 안전하게 추가

        // 요청 URL 파라미터 구성
        String requestUrl = TOKEN_URL + "?grant_type=authorization_code" +
                "&code=" + code;

        // API 호출 (GET 요청)
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 실제 통신 발생
        ResponseEntity<BandTokenResponse> response = restTemplate.exchange(
                requestUrl,
                HttpMethod.GET,
                entity,
                BandTokenResponse.class
        );

        return response.getBody();
    }
}