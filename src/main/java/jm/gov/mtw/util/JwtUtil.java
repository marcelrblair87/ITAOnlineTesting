package jm.gov.mtw.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.security.Key;
import java.util.Arrays;
import java.util.Date;

@Component
public class JwtUtil {

    private Key key;

    // Token validity: 1 hour
    private final long expirationMillis = 60 * 60 * 1000;

    @PostConstruct
    public void init() {
        // Use a securely generated key
        // You can persist and load this from secure storage in real apps
        this.key = Keys.secretKeyFor(SignatureAlgorithm.HS256);
    }

    public String generateToken(String trn, long expirationMillis) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMillis);

        return Jwts.builder()
                .setSubject(trn)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public boolean validateToken(String token, String expectedTrn) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return claims.getSubject().equals(expectedTrn) && !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String extractTrn(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractTrnFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("accessToken"))
                .findFirst()
                .map(Cookie::getValue)
                .map(this::extractTrn)
                .orElse(null);
    }

    public boolean validateTokenFromCookie(HttpServletRequest request) {
        if (request.getCookies() == null) return false;
        return Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("accessToken"))
                .findFirst()
                .map(Cookie::getValue)
                .map(token -> validateToken(token, extractTrn(token)))
                .orElse(false);
    }
}