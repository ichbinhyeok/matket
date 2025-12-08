package org.example.matket.domain.repository;

import java.util.Optional;
import org.example.matket.domain.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    Optional<Comment> findByBandCommentId(String bandCommentId);
}
