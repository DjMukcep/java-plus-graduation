package ru.practicum.dto.comment;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateCommentDto {
    @Size(max = 2000)
    private String text;

    private Long userId;
    private Long commentId;
}
