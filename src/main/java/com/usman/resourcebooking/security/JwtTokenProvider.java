package com.usman.resourcebooking.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.util.Date;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {

    private static final Logger log = LoggerFactory.getLogger(JwtTokenProvider.class);

    private static final String CLAIM_USER_ID = "userId";
    private static final String CLAIM_ROLE = "role";

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms:${jwt.expirationMs:86400000}}")
    private long jwtExpirationMs;

    private SecretKey signingKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(Authentication authentication) {
        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();

        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(principal.getUsername())
                .claim(CLAIM_USER_ID, principal.getId())
                .claim(CLAIM_ROLE, principal.getAuthorities()
                        .iterator().next().getAuthority()) // single-role model
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey()) // defaults to HS256
                .compact();
    }

    public String getUsernameFromJWT(String token) {
        return parseClaims(token).getSubject();
    }

    public Long getUserIdFromJWT(String token) {
        return parseClaims(token).get(CLAIM_USER_ID, Long.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean validateToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (SignatureException ex) {
            log.warn("Invalid JWT signature: {}", ex.getMessage());
        } catch (MalformedJwtException ex) {
            log.warn("Malformed JWT token: {}", ex.getMessage());
        } catch (ExpiredJwtException ex) {
            log.warn("Expired JWT token: {}", ex.getMessage());
        } catch (UnsupportedJwtException ex) {
            log.warn("Unsupported JWT token: {}", ex.getMessage());
        } catch (IllegalArgumentException ex) {
            log.warn("JWT claims string is empty or null: {}", ex.getMessage());
        }
        return false;
    }
}
