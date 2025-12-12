package org.example.matket.infra.naverBand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class BandPostDto {
    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_data")
    private ResultData resultData;

    @Data
    public static class ResultData {
        private List<Item> items;
    }

    @Data
    public static class Item {
        @JsonProperty("post_key")
        private String postKey;      // post_key -> postKey 로 변경

        private String content;

        @JsonProperty("created_at")
        private Long createdAt;      // created_at -> createdAt 로 변경

        private List<Photo> photos;
        private Author author;
    }

    @Data
    public static class Photo {
        private String url;

        @JsonProperty("is_video_thumbnail")
        private boolean isVideoThumbnail;
    }

    @Data
    public static class Author {
        private String name;
        private String role;

        @JsonProperty("user_key")
        private String userKey;
    }
}