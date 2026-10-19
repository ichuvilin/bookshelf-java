package com.ichuvilin.bookshelf.api.model.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequest {
    @Size(max = 50)
    @NotBlank(message = "username not be empty")
    private String username;
    @NotBlank(message = "email must not be blank")
    @Email(message = "email must be valid")
    @Size(max = 255, message = "email must not exceed 255 characters")
    private String email;
    @NotBlank(message = "password not be empty")
    @Size(min = 8, message = "password can't be min 8 characters")
    private String password;
}
