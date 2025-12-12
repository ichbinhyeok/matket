package org.example.matket.infra.naverBand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

// 댓글 목록 조회 결과 매핑용
@Data
public class BandCommentDto {
    @JsonProperty("result_data")
    private ResultData resultData;

    @JsonProperty("result_code")
    private int resultCode;

    @Data
    public static class ResultData {
        private List<Item> items;
        private Paging paging;
    }

    @Data
    public static class Item {
        private String content;      // 댓글 내용

        private Author author;       // 작성자 정보

        @JsonProperty("created_at")
        private long createdAt;      // 작성일 (Timestamp) -> Service에서 getCreatedAt() 호출 가능

        @JsonProperty("comment_key")
        private String commentKey;   // 댓글 고유 키 -> Service에서 getCommentKey() 호출 가능
    }

    @Data
    public static class Author {
        private String name;         // 작성자 이름

        @JsonProperty("profile_image_url")
        private String profileImageUrl; // 프로필 사진

        @JsonProperty("user_key")
        private String userKey;      // 유저 고유 키 -> Service에서 getUserKey() 호출 가능
    }

    @Data
    public static class Paging {
        @JsonProperty("next_params")
        private String nextParams; // 다음 댓글 조회용
    }
}