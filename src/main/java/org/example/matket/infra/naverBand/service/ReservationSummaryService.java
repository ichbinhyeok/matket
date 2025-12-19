package org.example.matket.infra.naverBand.service;

import lombok.RequiredArgsConstructor;
import org.example.matket.domain.Comment;
import org.example.matket.domain.enums.ParsedType;
import org.example.matket.domain.repository.CommentRepository;
import org.example.matket.infra.naverBand.dto.ReservationSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class ReservationSummaryService {

    private final CommentRepository commentRepository;
    private final Map<LocalDate, CacheEntry> cache = new ConcurrentHashMap<>();
    private static final Duration TTL = Duration.ofMinutes(5);

    @Transactional(readOnly = true)
    public ReservationSummaryDto getTodayReservationSummary(LocalDate date) {
        CacheEntry cached = cache.get(date);
        if (cached != null && !cached.isExpired()) {
            return cached.value();
        }

        List<Comment> comments = commentRepository.findByPost_PostDate(date);
        Map<String, Integer> aggregated = new HashMap<>();

        comments.stream()
                .filter(comment -> ParsedType.ORDER.equals(comment.getParsedType()))
                .forEach(comment -> mergeParsedData(comment.getParsedData(), aggregated));

        ReservationSummaryDto dto = new ReservationSummaryDto(date, aggregated);
        cache.put(date, new CacheEntry(dto, LocalDateTime.now().plus(TTL)));
        return dto;
    }

    public void evict(LocalDate date) {
        cache.remove(date);
    }

    public void evictAll() {
        cache.clear();
    }

    private void mergeParsedData(Map<String, Integer> parsedData, Map<String, Integer> aggregated) {
        if (parsedData == null || parsedData.isEmpty()) {
            return;
        }

        for (Map.Entry<String, Integer> entry : parsedData.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                continue;
            }
            aggregated.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }
    }

    private record CacheEntry(ReservationSummaryDto value, LocalDateTime expireAt) {
        boolean isExpired() {
            return LocalDateTime.now().isAfter(expireAt);
        }
    }
}
