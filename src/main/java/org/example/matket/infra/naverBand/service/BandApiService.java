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
    private final String BAND_POSTS_URL = "https://openapi.band.us/v2/band/posts?access_token=%s&band_key=%s&locale=ko_KR&limit=3";
    private final String BAND_COMMENTS_URL = "https://openapi.band.us/v2/band/post/comments?access_token=%s&band_key=%s&post_key=%s";
    private final String BAND_COMMENT_WRITE_URL = "https://openapi.band.us/v2/band/post/comment/create";

    // 토큰을 받아서 밴드 목록을 리턴하는 메서드
    public BandListResponse getBandList(String accessToken) {
        RestTemplate restTemplate = new RestTemplate();

        // 헤더에 토큰 집어넣기 (Bearer Token 방식)
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<String> entity = new HttpEntity<>(headers);

        // API 호출
        ResponseEntity<BandListResponse> response = restTemplate.exchange(
                BAND_LIST_URL,
                HttpMethod.GET,
                entity,
                BandListResponse.class
        );

        log.info("밴드 목록 조회 성공: {}", response.getBody());
        return response.getBody();
    }


    // 특정 밴드의 글 목록을 가져오는 기능
    public List<BandPostDto.Item> getBandPosts(String accessToken, String bandKey) {
        RestTemplate restTemplate = new RestTemplate();
        String requestUrl = String.format(BAND_POSTS_URL, accessToken, bandKey);

        try {
            // [핵심] getForObject를 사용하여 JSON을 DTO로 즉시 변환
            BandPostDto response = restTemplate.getForObject(requestUrl, BandPostDto.class);

            if (response != null && response.getResult_data() != null) {
                List<BandPostDto.Item> items = response.getResult_data().getItems();

                // 로그로 첫 번째 글 제목만 찍어보기 (확인용)
                if (!items.isEmpty()) {
                    log.info("첫 번째 글 본문 요약: {}", items.get(0).getContent().substring(0, 10) + "...");
                }

                return items;
            }
        } catch (Exception e) {
            log.error("API 호출 중 에러 발생: {}", e.getMessage());
        }
        return List.of(); // 빈 리스트 반환
    }

    /**
     * 3. [NEW] 댓글 목록 조회
     */
    public List<BandCommentDto.Item> getComments(String accessToken, String bandKey, String postKey) {
        RestTemplate restTemplate = new RestTemplate();
        // URL에 파라미터 조립
        String requestUrl = String.format(BAND_COMMENTS_URL, accessToken, bandKey, postKey);

        try {
            BandCommentDto response = restTemplate.getForObject(requestUrl, BandCommentDto.class);

            if (response != null && response.getResult_data() != null) {
                List<BandCommentDto.Item> items = response.getResult_data().getItems();
                log.info("댓글 조회 성공: {}개의 댓글", items.size());
                return items;
            }
        } catch (Exception e) {
            log.error("댓글 조회 실패: {}", e.getMessage());
        }
        return List.of();
    }

    /**
     * 4. [NEW] 댓글 작성
     * @return 성공 여부 (true/false)
     */
    public boolean writeComment(String accessToken, String bandKey, String postKey, String content) {
        RestTemplate restTemplate = new RestTemplate();

        // 1. 헤더 설정 (Form Data 전송)
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // 중요: 폼 데이터 형식

        // 2. 바디(파라미터) 설정
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("access_token", accessToken);
        params.add("band_key", bandKey);
        params.add("post_key", postKey);
        params.add("body", content);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

        try {
            // POST 요청 전송 (결과는 Map으로 받아서 성공 여부만 체크)
            ResponseEntity<Map> response = restTemplate.postForEntity(BAND_COMMENT_WRITE_URL, request, Map.class);

            Map<String, Object> body = response.getBody();
            if (body != null && body.get("result_code") != null) {
                int resultCode = (int) body.get("result_code");
                if (resultCode == 1) { // 1이면 성공
                    log.info("댓글 작성 성공! 내용: {}", content);
                    return true;
                }
            }
            log.warn("댓글 작성 실패 응답: {}", body);
            return false;

        } catch (Exception e) {
            log.error("댓글 작성 중 예외 발생: {}", e.getMessage());
            return false;
        }
    }
}