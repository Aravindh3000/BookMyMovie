package com.example.BookMyMovie.utils;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    private final String BASE_DIRECTORY = System.getProperty("user.dir") + "/keys";

    @Value("${JWT_ACCESS_TOKEN_VALIDITY}")
    private long JWT_ACCESS_TOKEN_VALIDITY;

    @Value("${JWT_REFRESH_TOKEN_VALIDITY}")
    private long JWT_REFRESH_TOKEN_VALIDITY;

    @Value("${STAGE}")
    private String stage;

    private PrivateKey getPrivateKey() {
        try {
            String keyPrefix = stage.equalsIgnoreCase("prod") ? "prod_" : "";
            String privateKeyPEM = new String(Files.readAllBytes(
                    Paths.get(BASE_DIRECTORY, keyPrefix, keyPrefix, "jwt_private_key.pem")))
                    .replace("-----BEGIN PRIVATE KEY-----", "")
                    .replace("-----END PRIVATE KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(privateKeyPEM);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load private key", e);
        }
    }

    private PublicKey getPublicKey() {
        try {
            String keyPrefix = stage.equalsIgnoreCase("prod") ? "prod_" : "";
            String publicKeyPEM = new String(Files.readAllBytes(
                    Paths.get(BASE_DIRECTORY, keyPrefix, "jwt_public_key.pem")))
                    .replace("-----BEGIN PUBLIC KEY-----", "")
                    .replace("-----END PUBLIC KEY-----", "")
                    .replaceAll("\\s+", "");

            byte[] decoded = Base64.getDecoder().decode(publicKeyPEM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decoded);
            return KeyFactory.getInstance("RSA").generatePublic(keySpec);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load public key", e);
        }
    }

    public String createAccessToken(Map<String, Object> claims, String subject) {
        return createAccessToken(claims, subject, JWT_ACCESS_TOKEN_VALIDITY);
    }

    public String createAccessToken(Map<String, Object> claims, String subject, Long validity) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + validity))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    public String createRefreshToken(String subject) {
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + JWT_REFRESH_TOKEN_VALIDITY))
                .signWith(getPrivateKey(), SignatureAlgorithm.RS256)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getPublicKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            System.out.println("ExpiredJwtException");
            return e.getClaims();
        } catch (JwtException e) {
            System.out.println("JwtException: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Invalid JWT token", e);
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public String extractRoleFromToken(String token) {
        return extractClaim(token, claims -> (String) claims.get("role"));
    }

    public boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            return true;
        }
    }

    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public boolean validateRefreshToken(String token) {
        try {
            System.out.println("validateRefreshToken");
            Date expiration = extractAllClaims(token).getExpiration();
            System.out.println("Expiration: " + expiration);

            return !isTokenExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public Map<String, Object> extractPayload(String token) {
        return extractAllClaims(token)
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    public Long getJwtAccessTokenValidity() {
        return JWT_ACCESS_TOKEN_VALIDITY;
    }
}
