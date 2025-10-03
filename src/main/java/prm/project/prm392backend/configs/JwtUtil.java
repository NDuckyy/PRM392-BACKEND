package prm.project.prm392backend.configs;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;
import java.util.Date;

public class JwtUtil {

    private static final String SECRET = "MySuperSecretKey1234567890MySuperSecretKey";
    private static final long EXPIRATION_TIME = 1000 * 60 * 60; // 1h

    // Sinh token
    public static String generateToken(String username, String role) {
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(username)
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
}

