package io.roa.secretmanger.Service;

import io.roa.secretmanger.DTO.request.Auth.LoginRequest;
import io.roa.secretmanger.DTO.response.LoginResponse;
import io.roa.secretmanger.DTO.request.Auth.RegisterRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    void register(RegisterRequest request);

    LoginResponse login(LoginRequest request, HttpServletResponse response);

    LoginResponse refresh(String refreshToken, HttpServletResponse response);

    void logout(HttpServletResponse response);
}
