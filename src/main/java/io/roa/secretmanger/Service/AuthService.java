package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.AuthDto;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void register(AuthDto.RegisterRequest request);

    AuthDto.LoginResponse login(AuthDto.LoginRequest request, HttpServletResponse response);

    AuthDto.LoginResponse refresh(String refreshToken, HttpServletResponse response);

    void logout(HttpServletResponse response);
}
