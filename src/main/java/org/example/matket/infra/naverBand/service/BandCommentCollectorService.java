package org.example.matket.infra.naverBand.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.matket.domain.Comment;
import org.example.matket.domain.Post;
import org.example.matket.domain.enums.ParsedType;
import org.example.matket.domain.repository.CommentRepository;
import org.example.matket.domain.repository.PostRepository;
import org.example.matket.infra.naverBand.dto.BandCommentDto;
import org.example.matket.infra.naverBand.dto.BandPostDto;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class BandCommentCollectorService {

    private final BandApiService bandApiService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Value("${band.test-access-token}")
    private String defaultAccessToken;

    @Value("${band.jorye-key}")
    private String defaultBandKey;

    @Transactional
    public void collectComments(String postKey) {
        collectComments(defaultBandKey, postKey);
    }

    @Transactional
    public void collectComments(String bandKey, String postKey) {
        collectComments(defaultAccessToken, bandKey, postKey);
    }

    @Transactional
    public void collectComments(String accessToken, String bandKey, String postKey) {
        // 1. 게시글(Post) 정보가 DB에 없으면 먼저 저장 (초기화)
        Post post = initializePost(accessToken, bandKey, postKey);

        // 2. 댓글 목록 가져오기
        List<BandCommentDto.Item> comments = bandApiService.getComments(accessToken, bandKey, postKey);

        for (BandCommentDto.Item item : comments) {
            // DTO 수정 후 getCommentKey() 사용 (CamelCase)
            Optional<Comment> existing = commentRepository.findByBandCommentKey(item.getCommentKey());
            if (existing.isPresent()) {
                continue;
            }

            Comment comment = Comment.builder()
                    .bandCommentKey(item.getCommentKey())
                    .post(post)
                    .authorKey(item.getAuthor().getUserKey())
                    .authorName(item.getAuthor().getName())
                    .content(item.getContent())
                    .parsedType(ParsedType.MISC)
                    .build();

            // 시간 설정 (Unix Timestamp -> LocalDateTime)
            comment.setOriginCreatedAt(item.getCreatedAt());

            commentRepository.save(comment);
        }
        log.info("댓글 수집 완료: 게시글({})에 대한 댓글 {}개 확인", postKey, comments.size());
    }

    @Transactional
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