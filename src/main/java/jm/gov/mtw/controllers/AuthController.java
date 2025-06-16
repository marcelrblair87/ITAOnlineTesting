package jm.gov.mtw.controllers;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jm.gov.mtw.dto.LoginRequest;
import jm.gov.mtw.dto.LoginResponse;
import jm.gov.mtw.dto.UserDto;
import jm.gov.mtw.models.User;
import jm.gov.mtw.repository.UserRepository;
import jm.gov.mtw.services.AuthService;
import jm.gov.mtw.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        boolean valid = authService.verifyUser(request.getTrn(), request.getFirstName(), request.getLastName());

        if (!valid) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid credentials"));
        }

        String accessToken = jwtUtil.generateToken(request.getTrn(), 10 * 60 * 1000); // 10 min
        String refreshToken = jwtUtil.generateToken(request.getTrn(), 24 * 60 * 60 * 1000); // 24 hours

        setCookie(response, "accessToken", accessToken, 600);
        setCookie(response, "refreshToken", refreshToken, 86400);

        return ResponseEntity.ok(new LoginResponse("Login successful"));
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = Arrays.stream(Optional.ofNullable(request.getCookies()).orElse(new Cookie[0]))
                .filter(c -> c.getName().equals("refreshToken"))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null || !jwtUtil.validateToken(refreshToken)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new LoginResponse("Invalid refresh token"));
        }

        String trn = jwtUtil.extractTrn(refreshToken);
        String newAccessToken = jwtUtil.generateToken(trn, 10 * 60 * 1000); // 10 min

        setCookie(response, "accessToken", newAccessToken, 600);
        return ResponseEntity.ok(new LoginResponse("Access token refreshed"));
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        clearCookie(response, "accessToken");
        clearCookie(response, "refreshToken");
        return ResponseEntity.ok(new LoginResponse("Logged out successfully"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> getCurrentUser(HttpServletRequest request) {
        String trn = jwtUtil.extractTrnFromCookie(request);
        if (trn == null || !jwtUtil.validateTokenFromCookie(request)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userRepository.findByTrn(trn)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return ResponseEntity.ok(UserDto.from(user));
    }

    private void clearCookie(HttpServletResponse response, String name) {
        ResponseCookie cookie = ResponseCookie.from(name, "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    private void setCookie(HttpServletResponse response, String name, String token, int maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Strict")
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
