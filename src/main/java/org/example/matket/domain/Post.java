package org.example.matket.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String bandPostKey;

    @Lob
    private String content;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDate postDate;

    @Builder.Default
    @Column(nullable = false)
    private boolean processed = false;

    @Lob
    private String productsDetected;

    private LocalDateTime lastCollectedAt;

    public void setOriginCreatedAt(Long unixMillis) {
        LocalDateTime originTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixMillis), ZoneId.systemDefault());
        this.createdAt = originTime;
        this.postDate = originTime.toLocalDate();
    }

    public void markCollectedAt(LocalDateTime collectedAt) {
        if (collectedAt == null) {
            return;
        }
        if (this.lastCollectedAt == null || collectedAt.isAfter(this.lastCollectedAt)) {
            this.lastCollectedAt = collectedAt;
        }
    }
}
