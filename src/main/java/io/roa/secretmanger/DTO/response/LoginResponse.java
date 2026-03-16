package io.roa.secretmanger.DTO.response;

public record LoginResponse(
        String accessToken,
        UserSummary user
) {
}
