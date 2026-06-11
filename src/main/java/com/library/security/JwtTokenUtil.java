package com.library.security;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * JWT Utility — uses jjwt 0.9.1 API
 *
 * BREAKS on jjwt 0.11+ upgrade:
 * - Jwts.builder().signWith(SignatureAlgorithm, key) → signWith(key, algorithm)
 * - Jwts.parser() → Jwts.parserBuilder()
 * - .setSigningKey(String) → .verifyWith(SecretKey)
 * - No more String keys — must use SecretKey (Keys.hmacShaKeyFor())
 * - .parseClaimsJws() → .parseSignedClaims()
 * - SignatureAlgorithm enum still works but method signatures differ
 * - All deprecated methods throw compile errors in 0.12+
 */
@Component
public class JwtTokenUtil {

    @Value("${jwt.secret}")
    private String secret;                         // Raw String secret — NOT valid in jjwt 0.11+

    @Value("${jwt.expiration}")
    private Long expiration;

    // Extract username from token
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    // Extract expiration date
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        // jjwt 0.9.x API: Jwts.parser().setSigningKey(String)
        // jjwt 0.11+  API: Jwts.parserBuilder().setSigningKey(SecretKey).build()
        // jjwt 0.12+  API: Jwts.parser().verifyWith(SecretKey).build()
        return null;          // ← REMOVED in 0.11+: must use SecretKey, not String       // ← REMOVED in 0.12+: use parseSignedClaims()
    }

    private Boolean isTokenExpired(String token) {
        final Date exp = getExpirationDateFromToken(token);
        return exp.before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", userDetails.getAuthorities());
        return doGenerateToken(claims, userDetails.getUsername());
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        // jjwt 0.9.x builder API
        // jjwt 0.11+: signWith(key, algorithm) — argument ORDER changed
        // jjwt 0.11+: String secret invalid — must wrap in Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret))
        return Jwts.builder()
                   .setClaims(claims)
                   .setSubject(subject)
                   .setIssuedAt(new Date(System.currentTimeMillis()))
                   .setExpiration(new Date(System.currentTimeMillis() + expiration * 1000))
                   .signWith(SignatureAlgorithm.HS512, secret)  // ← REMOVED in 0.11+: order flipped, String not allowed
                   .compact();
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
}
