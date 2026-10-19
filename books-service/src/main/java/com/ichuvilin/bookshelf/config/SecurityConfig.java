package com.ichuvilin.bookshelf.config;

import com.ichuvilin.bookshelf.auth.filter.JwtAuthenticationFilter;
import com.ichuvilin.bookshelf.integration.auth.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final AuthClient authClient;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.requestMatchers(
                                HttpMethod.GET, "/api/v1/books", "/api/v1/books/{id}", "/api/v1/books/{id}/reviews")
                        .permitAll()
                        .requestMatchers("/actuator/health")
                        .permitAll()
                        .anyRequest()
                        .authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(authClient), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
