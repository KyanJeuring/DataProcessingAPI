package com.fleetmaster.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.fleetmaster.security.JwtFilter;
import java.util.Arrays;

@Configuration
public class SecurityConfig {

  private final JwtFilter jwtFilter;

  public SecurityConfig(JwtFilter jwtFilter) {
    this.jwtFilter = jwtFilter;
  }

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration configuration = new CorsConfiguration();
    
    // Allow all localhost and 127.0.0.1 variations
    configuration.setAllowedOriginPatterns(Arrays.asList(
        "http://localhost*",
        "http://127.0.0.1*",
        "http://frontend*"
    ));
    
    configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH", "HEAD"));
    configuration.setAllowedHeaders(Arrays.asList("*"));
    configuration.setExposedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
    configuration.setAllowCredentials(true);
    configuration.setMaxAge(3600L);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", configuration);
    return source;
  }

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .authorizeHttpRequests(auth -> auth
            // Public endpoints - no authentication required
            .requestMatchers("/api/auth/register").permitAll()
            .requestMatchers("/api/auth/login").permitAll()
            .requestMatchers("/api/auth/api-register").permitAll()
            .requestMatchers("/api/auth/api-login").permitAll()
            .requestMatchers("/api/auth/verify/code/send").permitAll()
            .requestMatchers("/api/auth/verify/code/check").permitAll()
            .requestMatchers("/api/auth/password/recover").permitAll()
            .requestMatchers("/api/auth/password/reset").permitAll()
            .requestMatchers("/api/companies").permitAll()
            .requestMatchers("/api/companies/**").permitAll()
            .requestMatchers("/api/fleet/ping").permitAll()
            .requestMatchers("/error").permitAll()
            .requestMatchers("/api/docs/**").permitAll()
            .requestMatchers("/v3/api-docs/**").permitAll()
            .requestMatchers("/api/swagger-ui/**").permitAll()
            .requestMatchers("/swagger-ui/**").permitAll()
            
            // Protected endpoints - authentication required
            .requestMatchers("/api/auth/me").authenticated()
            .requestMatchers("/api/auth/**").authenticated()
            .requestMatchers("/api/orders/**").authenticated()
            .requestMatchers("/api/fleet/**").authenticated()
            .requestMatchers("/api/vehicles/**").authenticated()
            .requestMatchers("/api/info/**").authenticated()
            .requestMatchers("/database/info/**").authenticated()
            .requestMatchers("/api/weather/**").authenticated()
            .anyRequest().authenticated())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

    http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
    return http.build();
  }
}
