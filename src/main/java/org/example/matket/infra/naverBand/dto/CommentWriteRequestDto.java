package org.example.matket.infra.naverBand.dto;

import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class CommentWriteRequestDto {
    private String content; // 댓글 내용
}