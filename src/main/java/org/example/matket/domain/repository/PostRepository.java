package org.example.matket.domain.repository;

import java.util.Optional;
import org.example.matket.domain.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findByBandPostId(String bandPostId);
}
