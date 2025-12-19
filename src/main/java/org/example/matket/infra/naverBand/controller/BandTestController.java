package org.example.matket.infra.naverBand.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.matket.infra.naverBand.dto.*;
import org.example.matket.infra.naverBand.service.BandApiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Tag(name = "Naver Band API", description = "네이버 밴드 조회 및 댓글 관리 API")
@RestController
@RequiredArgsConstructor
public class BandTestController {

    private final BandApiService bandApiService;

    @Value("${band.test-access-token}")
    private String testAccessToken;

    @Value("${band.jorye-key}")
    private String targetBandKey;

    // --- 테스트용 간편 API ---

    @Operation(summary = "[테스트용] 내 밴드 목록 조회", description = "yml에 설정된 테스트 토큰을 사용하여 가입된 밴드 목록을 조회합니다.")
    @GetMapping("/test/bands")
    public BandListResponse showMyBands() {
        return bandApiService.getBandList(testAccessToken);
    }

    @Operation(summary = "[테스트용] 특정 밴드 글 목록 조회")
    @GetMapping("/test/posts")
    public List<BandPostDto.Item> showBandPosts() {
        return bandApiService.getBandPosts(testAccessToken, targetBandKey);
    }

    // --- 실제 서비스 API ---

    @Operation(summary = "댓글 목록 조회", description = "특정 밴드 게시글의 댓글 전체를 가져옵니다.")
    @GetMapping("/{bandKey}/posts/{postKey}/comments")
    public ResponseEntity<List<BandCommentDto.Item>> getComments(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String bandKey,
            @PathVariable String postKey) {

        String cleanToken = accessToken.replace("Bearer ", "");
        List<BandCommentDto.Item> all = new ArrayList<>();
        Map<String, String> nextParams = null;

        while (true) {
            BandCommentDto.ResultData resultData = bandApiService.getComments(cleanToken, bandKey, postKey, nextParams);
            if (resultData == null || resultData.getItems() == null || resultData.getItems().isEmpty()) {
                break;
            }
            all.addAll(resultData.getItems());
            nextParams = Optional.ofNullable(resultData.getPaging())
                    .map(BandCommentDto.Paging::getNextParams)
                    .orElse(null);
            if (nextParams == null || nextParams.isEmpty()) {
                break;
            }
        }
        return ResponseEntity.ok(all);
    }

    // (기존 일반 댓글 작성)
    @Operation(summary = "댓글 작성", description = "특정 게시글에 일반 댓글을 작성합니다.")
    @PostMapping("/{bandKey}/posts/{postKey}/comments")
    public ResponseEntity<String> writeComment(
            @Parameter(hidden = true)
            @RequestHeader("Authorization") String accessToken,
            @PathVariable String bandKey,
            @PathVariable String postKey,
            @RequestBody CommentWriteRequestDto requestDto
    ) {
        String cleanToken = accessToken.replace("Bearer ", "");
        boolean isSuccess = bandApiService.writeComment(cleanToken, bandKey, postKey, requestDto.getContent());

        if (isSuccess) {
            return ResponseEntity.ok("댓글 작성 성공");
        } else {
            return ResponseEntity.status(500).body("댓글 작성 실패");
        }
    }
}
