package prm.project.prm392backend.configs;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import prm.project.prm392backend.pojos.User;
import prm.project.prm392backend.repositories.UserRepository;

import java.text.ParseException;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private static final String SECRET = "MySuperSecretKey1234567890MySuperSecretKey";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1h
    private final UserRepository userRepository;

    public String generateToken(String username, String role) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
                    .claim("userId", user.getId())
                    .claim("role", role)
                    .expirationTime(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                    .issueTime(new Date())
                    .build();

            JWSSigner signer = new MACSigner(SECRET.getBytes());

            SignedJWT signedJWT = new SignedJWT(
                    new JWSHeader(JWSAlgorithm.HS256),
                    claims
            );

            signedJWT.sign(signer);

            return signedJWT.serialize();
        } catch (Exception e) {
            throw new RuntimeException("Error generating token", e);
        }
    }

    public static boolean validateToken(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            JWSVerifier verifier = new MACVerifier(SECRET.getBytes());
            return signedJWT.verify(verifier) && !isTokenExpired(signedJWT);
        } catch (Exception e) {
            return false;
        }
    }

    public static String extractUsername(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            return signedJWT.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean isTokenExpired(SignedJWT signedJWT) throws ParseException {
        Date expiration = signedJWT.getJWTClaimsSet().getExpirationTime();
        return expiration.before(new Date());
    }

    public static Integer extractUserId(String token) {
        try {
            SignedJWT signedJWT = SignedJWT.parse(token.replace("Bearer ", "").trim());
            Object idObj = signedJWT.getJWTClaimsSet().getClaim("userId");
            if (idObj == null) return null;
            if (idObj instanceof Number num) return num.intValue();
            return Integer.parseInt(idObj.toString());
        } catch (Exception e) {
            throw new RuntimeException("Cannot extract userId", e);
        }
    }
}
