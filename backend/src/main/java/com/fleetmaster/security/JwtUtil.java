package com.fleetmaster.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class JwtUtil {

  private final String SECRET = "fleetmastersecretfhsdifgsduzfktsdufgsdt34zu2gwjhdsjhfdscjym"; // change to environment variable
  private final long EXPIRATION = 86400000; // 1 day in ms

  public String generateToken(String subject) {
      return generateToken(subject, "COMPANY");
  }

  public String generateToken(String subject, String type) {
    return Jwts.builder()
        .setSubject(subject)
        .claim("type", type)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION))
        .signWith(SignatureAlgorithm.HS256, SECRET)
        .compact();
  }

  public String extractSubject(String token) {
    return getClaims(token).getSubject();
  }

  // Deprecated wrapper for backward compatibility if needed, or just rename usages
  public String extractEmail(String token) {
    return extractSubject(token);
  }

  public String extractType(String token) {
    Claims claims = getClaims(token);
    return (String) claims.get("type");
  }

  public boolean validateToken(String token, String subject) {
    String tokenSubject = extractSubject(token);
    return tokenSubject.equals(subject) && !isTokenExpired(token);
  }

  private boolean isTokenExpired(String token) {
    return getClaims(token).getExpiration().before(new Date());
  }

  private Claims getClaims(String token) {
    return Jwts.parser()
        .setSigningKey(SECRET)
        .parseClaimsJws(token)
        .getBody();
  }
}
