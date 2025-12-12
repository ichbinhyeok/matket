package org.example.matket.domain.repository;

import org.example.matket.domain.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Optional<Comment> findByBandCommentKey(String bandCommentKey);
    List<Comment> findByPost_PostDate(LocalDate postDate);

}

