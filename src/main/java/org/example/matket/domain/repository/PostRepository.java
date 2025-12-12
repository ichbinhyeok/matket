package org.example.matket.domain.repository;

import org.example.matket.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    Optional<Post> findByBandPostKey(String bandPostKey);

    List<Post> findByPostDate(LocalDate date);
}