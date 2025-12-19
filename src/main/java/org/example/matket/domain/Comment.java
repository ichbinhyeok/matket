package org.example.matket.domain;

import jakarta.persistence.*;
import lombok.*;
import org.example.matket.domain.OrderItem;
import org.example.matket.domain.converter.JsonMapConverter;
import org.example.matket.domain.enums.ParsedType;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    @Builder.Default
    @Enumerated(EnumType.STRING)
    private ParsedType parsedType = ParsedType.MISC;

    @Builder.Default
    @Lob
    @Convert(converter = JsonMapConverter.class)
    private Map<String, Integer> parsedData = new LinkedHashMap<>();

    @Builder.Default
    @OneToMany(mappedBy = "comment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    public void setOriginCreatedAt(Long unixMillis) {
        this.createdAt = LocalDateTime.ofInstant(Instant.ofEpochMilli(unixMillis), ZoneId.systemDefault());
    }

    public void setParsedData(Map<String, Integer> parsedData) {
        this.parsedData = parsedData == null ? new LinkedHashMap<>() : new LinkedHashMap<>(parsedData);
    }

    public void syncOrderItemsFromParsedData() {
        if (parsedData == null || parsedData.isEmpty()) {
            orderItems.clear();
            return;
        }
        orderItems.clear();
        parsedData.forEach((product, qty) -> orderItems.add(new OrderItem(this, product, qty)));
    }
}
