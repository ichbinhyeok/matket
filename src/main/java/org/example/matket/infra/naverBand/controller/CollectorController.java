package org.example.matket.infra.naverBand.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matket.infra.naverBand.service.BandCommentCollectorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Tag(
        name = "Band Comment Collector",
        description = "네이버 밴드 댓글 수집용 MVP API"
)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class CollectorController {

    private final BandCommentCollectorService collectorService;

    @Value("${band.jorye-key}")
    private String defaultBandKey;

    @Operation(
            summary = "밴드 댓글 수집 트리거",
            description = """
            특정 밴드 게시글의 댓글을 수집하여 DB에 저장합니다.
            
            - 이미 수집된 댓글은 자동으로 스킵됩니다 (idempotent)
            - Authorization 헤더가 없으면 기본 테스트 토큰을 사용합니다
            - MVP 단계에서는 파싱 없이 원본 데이터만 저장합니다
            """
    )
    @PostMapping("/collect/{postKey}")
    public ResponseEntity<String> collectComments(

            @Parameter(
                    description = "밴드 게시글 고유 키",
                    example = "AACNHc0vCG_I0aPnzsMr3ut3",
                    required = true
            )
            @PathVariable String postKey,

            @Parameter(
                    description = "밴드 키 (미입력 시 기본 band key 사용)",
                    example = "AAAbdB7Veb_-M1lwHrYlU5E7"
            )
            @RequestParam(value = "bandKey", required = false) String bandKey,

            @Parameter(
                    description = "Bearer Access Token (미입력 시 application.yml의 test token 사용)",
                    example = "Bearer ZQAAAZ..."
            )
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        String targetBandKey = Optional.ofNullable(bandKey).orElse(defaultBandKey);
        String accessToken = extractBearerToken(authorizationHeader).orElse(null);

        if (accessToken != null) {
            collectorService.collectComments(accessToken, targetBandKey, postKey);
        } else {
            collectorService.collectComments(targetBandKey, postKey);
        }

        return ResponseEntity.ok("Collection triggered");
    }

    private Optional<String> extractBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(authorizationHeader.replace("Bearer ", ""));
    }
}
