package org.example.matket.infra.naverBand.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matket.infra.naverBand.dto.BandCommentDto;
import org.example.matket.infra.naverBand.dto.BandListResponse;
import org.example.matket.infra.naverBand.dto.BandPostDto;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BandApiService {

    private final String BAND_LIST_URL = "https://openapi.band.us/v2.1/bands";
    private final String BAND_POSTS_URL = "https://openapi.band.us/v2/band/posts?access_token=%s&band_key=%s&locale=ko_KR";
    private final String BAND_COMMENTS_URL = "https://openapi.band.us/v2/band/post/comments?access_token=%s&band_key=%s&post_key=%s";
    private final String BAND_COMMENT_WRITE_URL = "https://openapi.band.us/v2/band/post/comment/create";

    // ... (기존 getBandList, getBandPosts, getComments 메서드는 그대로 둠) ...

    public BandListResponse getBandList(String accessToken) {
        // (기존 코드 생략 - 위와 동일)
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        ResponseEntity<BandListResponse> response = restTemplate.exchange(BAND_LIST_URL, HttpMethod.GET, entity, BandListResponse.class);
        return response.getBody();
    }

    public List<BandPostDto.Item> getBandPosts(String accessToken, String bandKey) {
        // (기존 코드 생략 - 위와 동일)
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(BAND_POSTS_URL, accessToken, bandKey);
        try {
            BandPostDto response = restTemplate.getForObject(requestUrl, BandPostDto.class);
            if (response != null && response.getResult_code() == 1 && response.getResult_data() != null) {
                return response.getResult_data().getItems();
            }
        } catch (Exception e) {
            log.error("API 호출 중 에러 발생: {}", e.getMessage());
        }
        return List.of();
    }

    public List<BandCommentDto.Item> getComments(String accessToken, String bandKey, String postKey) {
        // (기존 코드 생략 - 위와 동일)
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(BAND_COMMENTS_URL, accessToken, bandKey, postKey);
        try {
            BandCommentDto response = restTemplate.getForObject(requestUrl, BandCommentDto.class);
            if (response != null && response.getResult_data() != null) {
                return response.getResult_data().getItems();
            }
        } catch (Exception e) {
            log.error("댓글 조회 실패: {}", e.getMessage());
        }
        return List.of();
    }

    // ... (기존 writeComment 메서드 그대로 유지) ...
    public boolean writeComment(String accessToken, String bandKey, String postKey, String content) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", accessToken);
        params.add("band_key", bandKey);
        params.add("post_key", postKey);
        params.add("body", content);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(BAND_COMMENT_WRITE_URL, request, Map.class);
            Map<String, Object> body = response.getBody();
            if (body != null && body.get("result_code") != null) {
                int resultCode = (int) body.get("result_code");
                if (resultCode == 1) {
                    log.info("댓글 작성 성공! 내용: {}", content);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("댓글 작성 중 예외 발생: {}", e.getMessage());
            return false;
        }
    }
}