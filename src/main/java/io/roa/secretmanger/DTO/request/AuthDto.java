package io.roa.secretmanger.DTO.request;


import io.roa.secretmanger.Model.Value.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.UUID;


public class AuthDto {

    public record RegisterRequest(
            @NotBlank @Size(max = 100)
            String name,

            @NotBlank @Email @Size(max = 150)
            String email,

            @NotBlank @Size(min = 8, max = 100)
            String password,

            UserRole role
    ) {
    }

    public record LoginRequest(
            @NotBlank @Email
            String email,

            @NotBlank
            @NotBlank @Size(min = 8, max = 100)
            String password
    ) {
    }


    public record LoginResponse(
            String accessToken,
            UserSummary user
    ) {
    }

    public record UserSummary(
            UUID id,
            String name,
            String email,
            UserRole role
    ) {
    }
}
