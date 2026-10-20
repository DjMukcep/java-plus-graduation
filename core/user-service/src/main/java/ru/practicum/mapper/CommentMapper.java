package ru.practicum.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.dto.comment.UserCommentAdminDto;
import ru.practicum.entity.User;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class CommentMapper {

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    public static UserCommentAdminDto toUserCommentAdminDto(User user) {
        return UserCommentAdminDto.builder()
                .authorId(user.getId())
                .authorName(user.getName())
                .adminWarnCount(user.getAdminWarnings())
                .bannedUntil(user.getBannedUntil() != null ? user.getBannedUntil().format(formatter) : null)
                .banned(user.isBanned())
                .commentCount(user.getCommentsCount())
                .build();
    }
}
