package org.example.matket.infra.naverBand.dto;

import lombok.Data;
import java.util.List;

@Data
public class BandPostDto {
    private ResultData result_data;
    private int result_code;


    @Data
    public static class ResultData {
        private List<Item> items;
    }

    @Data
    public static class Item {
        private String content;      // 글 본문
        private Long created_at;     // 작성일 (Timestamp)
        private String post_key;     // 게시글 고유 키
        private List<Photo> photos;  // 사진 리스트
        private Author author;       // 작성자 정보
    }

    @Data
    public static class Photo {
        private String url;          // 이미지 URL (핵심)
        private boolean is_video_thumbnail;
    }

    @Data
    public static class Author {
        private String name;
        private String role;
    }
}