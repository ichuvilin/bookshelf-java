package com.ichuvilin.bookshelf.api.model.response;

import com.ichuvilin.bookshelf.domain.UserPublic;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String accessToken;
    private String tokenType;
    private Integer expiresIn;
    private UserPublic user;
}
