package org.example.matket.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.matket.domain.enums.ParsedType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bandCommentKey;

    @ManyToOne(optional = false)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    private String authorName;

    @Column(nullable = false)
    private String authorKey;

    @Lob
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    private ParsedType parsedType;

    @Lob
    private String parsedData;

    public void setOriginCreatedAt(Long unixMillis) {
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixMillis), ZoneId.systemDefault());
    }
}
