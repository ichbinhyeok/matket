package org.example.matket.infra.naverBand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class BandPostDetailDto {
    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_data")
    private ResultData resultData;

    @Data
    public static class ResultData {
        // 목록 조회와 달리 'post'라는 단일 객체로 내려옵니다.
        private BandPostDto.Item post;
    }
}