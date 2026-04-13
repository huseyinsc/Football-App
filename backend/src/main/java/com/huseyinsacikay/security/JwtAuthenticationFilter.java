package com.huseyinsacikay.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        log.info("Filter Process: Request to [{}] encountered.", request.getRequestURI());
        log.info("Incoming Authorization Header from Client: [{}]", authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.info("No 'Bearer ' token found in Authorization header. Proceeding without authenticating.");
            filterChain.doFilter(request, response);
            return;
        }

        String extractedJwt = authHeader.substring(7).trim();
        // Fallback for Swagger UI prepending "Bearer " automatically
        if (extractedJwt.startsWith("Bearer ")) {
            log.warn("Detected duplicate 'Bearer ' prefix (common Swagger UI issue). Stripping it.");
            extractedJwt = extractedJwt.substring(7).trim();
        }
        jwt = extractedJwt;
        log.info("Token extracted and ready for validation: [{}]", jwt);

        try {
            username = jwtService.extractUsername(jwt);
            log.info("Successfully parsed token. Username subject is: [{}]", username);
        } catch (JwtException | IllegalArgumentException ex) {
            log.error("Failed to parse the token! Is it a valid JWT string? Error: {}", ex.getMessage());
            SecurityContextHolder.clearContext();
            filterChain.doFilter(request, response);
            return;
        }
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            try {
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);
                if (jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities());
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            } catch (UsernameNotFoundException ex) {
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }
}
