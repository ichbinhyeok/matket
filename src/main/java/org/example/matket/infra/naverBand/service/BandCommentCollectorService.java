package org.example.matket.infra.naverBand.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matket.domain.Comment;
import org.example.matket.domain.Post;
import org.example.matket.domain.enums.ParsedType;
import org.example.matket.domain.repository.CommentRepository;
import org.example.matket.domain.repository.OrderItemRepository;
import org.example.matket.domain.repository.PostRepository;
import org.example.matket.infra.naverBand.dto.BandCommentDto;
import org.example.matket.infra.naverBand.dto.BandPostDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BandCommentCollectorService {

    private final BandApiService bandApiService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final OrderItemRepository orderItemRepository;
    private final ReservationSummaryService reservationSummaryService;
    private final PlatformTransactionManager transactionManager;

    @Value("${band.test-access-token}")
    private String defaultAccessToken;

    @Value("${band.jorye-key}")
    private String defaultBandKey;

    public void collectComments(String postKey) {
        collectComments(defaultBandKey, postKey);
    }

    public void collectComments(String bandKey, String postKey) {
        collectComments(defaultAccessToken, bandKey, postKey);
    }

    public void collectComments(String accessToken, String bandKey, String postKey) {
        Post post = initializePost(accessToken, bandKey, postKey);
        LocalDateTime lastCollectedAt = post.getLastCollectedAt();

        Map<String, String> nextParams = null;
        LocalDateTime latestCollected = lastCollectedAt;

        while (true) {
            BandCommentDto.ResultData resultData = bandApiService.getComments(accessToken, bandKey, postKey, nextParams);
            if (resultData == null || resultData.getItems() == null) {
                log.warn("댓글 페이지 수집 중 네트워크 오류 또는 응답 없음. postKey={}", postKey);
                break;
            }

            List<Comment> batch = new ArrayList<>();
            for (BandCommentDto.Item item : resultData.getItems()) {
                LocalDateTime createdAt = toLocalDateTime(item.getCreatedAt());
                if (lastCollectedAt != null && !createdAt.isAfter(lastCollectedAt)) {
                    continue;
                }

                Optional<Comment> existing = commentRepository.findByBandCommentKey(item.getCommentKey());
                if (existing.isPresent()) {
                    continue;
                }

                ParseResult parseResult = parseOrderContent(item.getContent());
                Comment comment = Comment.builder()
                        .bandCommentKey(item.getCommentKey())
                        .post(post)
                        .authorKey(item.getAuthor().getUserKey())
                        .authorName(item.getAuthor().getName())
                        .content(item.getContent())
                        .parsedType(parseResult.parsedType())
                        .parsedData(parseResult.data())
                        .build();

                comment.setOriginCreatedAt(item.getCreatedAt());
                comment.syncOrderItemsFromParsedData();
                batch.add(comment);

                if (latestCollected == null || createdAt.isAfter(latestCollected)) {
                    latestCollected = createdAt;
                }
            }

            persistBatch(batch);

            nextParams = Optional.ofNullable(resultData.getPaging())
                    .map(BandCommentDto.Paging::getNextParams)
                    .orElse(null);

            if (nextParams == null || nextParams.isEmpty()) {
                break;
            }
        }

        if (latestCollected != null) {
            post.markCollectedAt(latestCollected);
            postRepository.save(post);
            reservationSummaryService.evict(post.getPostDate());
        }
        log.info("댓글 수집 완료: 게시글({})에 대한 최신 수집 시각 {}", postKey, latestCollected);
    }

    private void persistBatch(List<Comment> batch) {
        if (batch.isEmpty()) {
            return;
        }

        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.executeWithoutResult(status -> {
            batch.forEach(comment -> {
                commentRepository.save(comment);
                if (ParsedType.ORDER.equals(comment.getParsedType()) && !comment.getOrderItems().isEmpty()) {
                    orderItemRepository.saveAll(comment.getOrderItems());
                }
            });
        });
    }

    private ParseResult parseOrderContent(String content) {
        if (content == null || content.isBlank()) {
            return ParseResult.empty();
        }

        Map<String, Integer> parsed = new LinkedHashMap<>();
        String[] tokens = content.split("[\\n,]");
        for (String token : tokens) {
            String[] colonSplit = token.split(":", 2);
            if (colonSplit.length == 2 && isNumeric(colonSplit[1].trim())) {
                parsed.merge(colonSplit[0].trim(), Integer.parseInt(colonSplit[1].trim()), Integer::sum);
                continue;
            }

            String[] xSplit = token.split("[xX*]", 2);
            if (xSplit.length == 2 && isNumeric(xSplit[1].trim())) {
                parsed.merge(xSplit[0].trim(), Integer.parseInt(xSplit[1].trim()), Integer::sum);
            }
        }

        if (parsed.isEmpty()) {
            return ParseResult.empty();
        }
        return new ParseResult(ParsedType.ORDER, parsed);
    }

    private boolean isNumeric(String value) {
        try {
            Integer.parseInt(value);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private LocalDateTime toLocalDateTime(Long unixMillis) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(unixMillis), ZoneId.systemDefault());
    }

    private record ParseResult(ParsedType parsedType, Map<String, Integer> data) {
        static ParseResult empty() {
            return new ParseResult(ParsedType.MISC, Map.of());
        }
    }

    public Post initializePost(String accessToken, String bandKey, String postKey) {
        return postRepository.findByBandPostKey(postKey)
                .orElseGet(() -> createPostFromApi(accessToken, bandKey, postKey));
    }

    private Post createPostFromApi(String accessToken, String bandKey, String postKey) {
        // [수정] 목록 조회 대신 '단건 상세 조회' 메서드 호출
        BandPostDto.Item apiPost = fetchPost(accessToken, bandKey, postKey)
                .orElseThrow(() -> new IllegalArgumentException("Post not found from Band API: " + postKey));

        Post post = Post.builder()
                .bandPostKey(apiPost.getPostKey())
                .content(apiPost.getContent())
                .build();

        post.setOriginCreatedAt(apiPost.getCreatedAt());
        return postRepository.save(post);
    }

    private Optional<BandPostDto.Item> fetchPost(String accessToken, String bandKey, String postKey) {
        // [핵심 변경] 리스트 루프 대신 단건 조회 API 사용
        BandPostDto.Item item = bandApiService.getPostDetail(accessToken, bandKey, postKey);
        return Optional.ofNullable(item);
    }
}
