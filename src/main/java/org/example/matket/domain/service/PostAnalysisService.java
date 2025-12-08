package org.example.matket.domain.service;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.example.matket.domain.entity.Post;
import org.example.matket.domain.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostAnalysisService {

    private final PostRepository postRepository;

    @Transactional
    public Post ingestPost(PostPayload payload) {
        Optional<Post> existing = postRepository.findByBandPostId(payload.bandPostId());
        if (existing.isPresent()) {
            return existing.get();
        }

        Post post = Post.builder()
                .bandPostId(payload.bandPostId())
                .bandId(payload.bandId())
                .authorName(payload.authorName())
                .content(payload.content())
                .postedAt(payload.postedAt())
                .productParsingRequested(false)
                .build();
        Post saved = postRepository.save(post);

        requestProductParsing(saved);
        return saved;
    }

    /**
     * Stub for LLM-based product extraction. Actual integration will call the LLM asynchronously
     * and persist parsed {@link org.example.matket.domain.entity.Product} instances.
     */
    protected void requestProductParsing(Post post) {
        post.setProductParsingRequested(true);
        // TODO: enqueue request to LLM service for product parsing
    }

    public record PostPayload(String bandPostId, String bandId, String authorName, String content,
                              java.time.LocalDateTime postedAt) {
    }
}
