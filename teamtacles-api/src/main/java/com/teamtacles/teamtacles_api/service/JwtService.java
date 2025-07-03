package com.teamtacles.teamtacles_api.service;

import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import com.teamtacles.teamtacles_api.model.User;

/**
 * Service class responsible for generating JSON Web Tokens (JWTs) for authenticated users
 * in the TeamTacles application. It utilizes Spring Security's {JwtEncoder
 * to create signed and time-limited tokens containing user-specific claims.
 *
 * @author TeamTacles
 * @version 1.0
 * @since 2025-05-25
 */
@Service
public class JwtService {
    private final JwtEncoder jwtEncoder;

    public JwtService(JwtEncoder encoder) {
        this.jwtEncoder = encoder;
    }

    /**
     * Generates a JSON Web Token (JWT) for a given User.
     * The token includes standard claims such as issuer, issued at, expiration time,
     * subject (username), and a custom claim for the user's ID.
     * The token is set to expire 3600 seconds (1 hour) from its issue time.
     *
     * @param user The User for whom the token is to be generated.
     * @return A String representing the encoded JWT.
     */
    public String generateToken(User user) {
        Instant now = Instant.now();
        long expire = 3600L;

        var claims = JwtClaimsSet.builder()
                .issuer("spring-security")
                .issuedAt(now)
                .expiresAt(now.plusSeconds(expire))
                .subject(user.getUserName())
                .claim("userId", user.getUserId())
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }
}
