package io.roa.secretmanger.Service.Impl;

import io.roa.secretmanger.DTO.request.Auth.LoginRequest;
import io.roa.secretmanger.DTO.request.Auth.RegisterRequest;
import io.roa.secretmanger.DTO.response.LoginResponse;
import io.roa.secretmanger.Exception.DuplicateResourceException;
import io.roa.secretmanger.Exception.ResourceNotFoundException;
import io.roa.secretmanger.Exception.UnauthorizedException;
import io.roa.secretmanger.Mapper.UserMapper;
import io.roa.secretmanger.Model.Entity.User;
import io.roa.secretmanger.Model.Value.UserRole;
import io.roa.secretmanger.Repo.UserRepo;
import io.roa.secretmanger.Service.AuthService;
import io.roa.secretmanger.Service.ShamirService;
import io.roa.secretmanger.Util.CookieUtil;
import io.roa.secretmanger.Util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepo userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final UserMapper userMapper;
    private final ShamirService shamirService;
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateResourceException("Email already in use");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        userRepository.save(user);

        if (request.role() == UserRole.ADMIN) {
            shamirService.splitAndDistribute();
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request, HttpServletResponse response) {
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        User user = (User) auth.getPrincipal();

        String accessToken = jwtUtil.generateAccessToken(user);
        String refreshToken = jwtUtil.generateRefreshToken(user);

        cookieUtil.addRefreshTokenCookie(response, refreshToken);

        return new LoginResponse(accessToken, userMapper.toDto(user),
                jwtUtil.getExpirationFromToken(accessToken).getTime());
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {
        if (!jwtUtil.isRefreshToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        String email = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (!jwtUtil.validateToken(refreshToken, user)) {
            throw new UnauthorizedException("Refresh token expired");
        }

        String newAccessToken = jwtUtil.generateAccessToken(user);
        String newRefreshToken = jwtUtil.generateRefreshToken(user);

        cookieUtil.addRefreshTokenCookie(response, newRefreshToken);

        return new LoginResponse(newAccessToken, userMapper.toDto(user),jwtUtil.getExpirationFromToken(newAccessToken).getTime());
    }

    public void logout(HttpServletResponse response) {
        cookieUtil.deleteRefreshTokenCookie(response);
    }
}