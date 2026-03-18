package io.roa.secretmanger.Controller;

import io.roa.secretmanger.Controller.docs.AuthEndpointDoc;
import io.roa.secretmanger.DTO.request.Auth.LoginRequest;
import io.roa.secretmanger.DTO.request.Auth.RegisterRequest;
import io.roa.secretmanger.DTO.response.ApiRes;
import io.roa.secretmanger.DTO.response.LoginResponse;
import io.roa.secretmanger.Service.AuthService;
import io.roa.secretmanger.Util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthEndpointDoc {

    private final AuthService authService;
    private final CookieUtil cookieUtil;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiRes<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ApiRes.success("User registered successfully", null);
    }

    @PostMapping("/login")
    public ApiRes<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                       HttpServletResponse response) {
        return ApiRes.success(authService.login(request, response));
    }

    @PostMapping("/refresh")
    public ApiRes<LoginResponse> refresh(HttpServletRequest request,
                                         HttpServletResponse response) {
        String refreshToken = cookieUtil.getRefreshTokenFromCookie(request)
                .orElseThrow(() -> new io.roa.secretmanger.Exception.UnauthorizedException(
                        "No refresh token found"));
        return ApiRes.success(authService.refresh(refreshToken, response));
    }

    @PostMapping("/logout")
    public ApiRes<Void> logout(HttpServletResponse response) {
        authService.logout(response);
        return ApiRes.success("Logged out successfully", null);
    }
}