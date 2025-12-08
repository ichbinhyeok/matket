package org.example.matket.infra.naverBand.dto;

import lombok.Data;
import java.util.List;

// 댓글 목록 조회 결과 매핑용
@Data
public class BandCommentDto {
    private ResultData result_data;

    @Data
    public static class ResultData {
        private List<Item> items;
        private Paging paging;
    }

    @Data
    public static class Item {
        private String content;      // 댓글 내용
        private Author author;       // 작성자 정보
        private long created_at;     // 작성일 (Timestamp)
    }

    @Data
    public static class Author {
        private String name;         // 작성자 이름
        private String profile_image_url; // 프로필 사진
        private String user_key;     // 유저 고유 키
    }

    @Data
    public static class Paging {
        private String next_params; // 다음 댓글 조회용
    }
}