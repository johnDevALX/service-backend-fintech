package com.ekene.onlinebookstore.auth;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final CustomUserService customUserService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");

        String jwtToken = null;
        String userEmail = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            jwtToken = authorizationHeader.substring(7);
            userEmail = jwtUtil.extractUsername(jwtToken);
        }

        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null){
            UserDetails userDetails = customUserService.loadUserByUsername(userEmail);
            if (jwtUtil.isTokenValid(jwtToken, userDetails)){
                UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                usernamePasswordAuthenticationToken
                        .setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
            }
        }
        filterChain.doFilter(request, response);
    }
}
