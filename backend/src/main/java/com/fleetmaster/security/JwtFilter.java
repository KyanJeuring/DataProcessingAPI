package com.fleetmaster.security;

import com.fleetmaster.entities.CompanyAccount;
import com.fleetmaster.entities.ApiAccount;
import com.fleetmaster.services.AuthService;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

  private final JwtUtil jwtUtil;
  private final AuthService authService;

  public JwtFilter(JwtUtil jwtUtil, AuthService authService) {
    this.jwtUtil = jwtUtil;
    this.authService = authService;
  }

  @Override
  protected void doFilterInternal(HttpServletRequest request,
      HttpServletResponse response,
      FilterChain filterChain) throws ServletException, IOException {

    // Allow CORS preflight requests to pass through without JWT validation
    if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
      filterChain.doFilter(request, response);
      return;
    }

    String authHeader = request.getHeader("Authorization");

    if (authHeader != null && authHeader.startsWith("Bearer ")) {
      String token = authHeader.substring(7);
      try {
        String subject = jwtUtil.extractSubject(token);
        String type = jwtUtil.extractType(token);

        if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
          if ("API".equals(type)) {
               ApiAccount apiAccount = authService.getApiAccountByUsername(subject);
               if (apiAccount != null && jwtUtil.validateToken(token, apiAccount.getUsername())) {
                 // For now, no roles/authorities for API users, or we can add a simple "ROLE_API"
                 UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                         apiAccount, null, Collections.emptyList());
                 authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                 SecurityContextHolder.getContext().setAuthentication(authToken);
               }
          } else {
             // Default to COMPANY if type is missing or matches
              CompanyAccount companyAccount = authService.getCompanyAccountByEmail(subject);
              if (companyAccount != null && jwtUtil.validateToken(token, companyAccount.getEmail())) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    companyAccount, null, Collections.emptyList());
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
              }
          }
        }
      } catch (Exception ignored) {
      }
    }

    filterChain.doFilter(request, response);
  }
}
