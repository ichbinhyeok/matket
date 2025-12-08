package org.example.matket.infra.naverBand.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.ToString;
import java.util.List;

@Getter
@ToString
public class BandListResponse {
    @JsonProperty("result_code")
    private int resultCode;

    @JsonProperty("result_data")
    private ResultData resultData;

    @Getter
    @ToString
    public static class ResultData {
        private List<BandInfo> bands;
    }

    @Getter
    @ToString
    public static class BandInfo {
        private String name;       // 밴드 이름
        @JsonProperty("band_key")
        private String bandKey;    // ★ 제일 중요: 이 밴드의 고유 ID
        @JsonProperty("cover")
        private String coverUrl;   // 밴드 커버 이미지
        @JsonProperty("member_count")
        private int memberCount;
    }
}