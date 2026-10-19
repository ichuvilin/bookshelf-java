package com.ichuvilin.bookshelf.domain;

import com.ichuvilin.bookshelf.db.entity.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPublic {
    private Long id;
    private String username;
    private String email;
    private Instant createdAt;
    private Instant updatedAt;

    public static UserPublic toPublic(User user) {
        return UserPublic.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
