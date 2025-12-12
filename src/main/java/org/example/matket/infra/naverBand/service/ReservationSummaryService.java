package org.example.matket.infra.naverBand.service;

import lombok.RequiredArgsConstructor;
import org.example.matket.domain.Comment;
import org.example.matket.domain.enums.ParsedType;
import org.example.matket.domain.repository.CommentRepository;
import org.example.matket.infra.naverBand.dto.ReservationSummaryDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReservationSummaryService {

    private final CommentRepository commentRepository;

    @Transactional(readOnly = true)
    public ReservationSummaryDto getTodayReservationSummary(LocalDate date) {
        List<Comment> comments = commentRepository.findByPost_PostDate(date);
        Map<String, Integer> aggregated = new HashMap<>();

        comments.stream()
                .filter(comment -> ParsedType.ORDER.equals(comment.getParsedType()))
                .forEach(comment -> mergeParsedData(comment, aggregated));

        return new ReservationSummaryDto(date, aggregated);
    }

    private void mergeParsedData(Comment comment, Map<String, Integer> aggregated) {
        if (comment.getParsedData() == null || comment.getParsedData().isBlank()) {
            return;
        }

        String[] lines = comment.getParsedData().split("\n");
        for (String line : lines) {
            String[] parts = line.split(":", 2);
            if (parts.length < 2) {
                continue;
            }
            String productName = parts[0].trim();
            try {
                int quantity = Integer.parseInt(parts[1].trim());
                aggregated.merge(productName, quantity, Integer::sum);
            } catch (NumberFormatException ignored) {
                // Skip lines that are not properly formatted
            }
        }
    }
}