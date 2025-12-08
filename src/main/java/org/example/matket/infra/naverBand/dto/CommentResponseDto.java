package org.example.matket.infra.naverBand.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CommentResponseDto {
    private String authorName;  // 작성자 이름
    private String content;     // 댓글 내용
    private String createdAt;   // 작성일
    private boolean isMyComment; // 내가 쓴 댓글인지 여부
}