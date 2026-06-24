package com.CheckMate.checkmate_server.security.jwt;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private static final String LOGIN_URL = "/api/v1/user/auth/login";
    private static final long TOKEN_EXPIRE_MS = 60 * 60 * 1000L;

    private final AuthenticationManager authenticationManager;
    private final JWTUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public LoginFilter(AuthenticationManager authenticationManager, JWTUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        setFilterProcessesUrl(LOGIN_URL);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        LoginRequest loginRequest = resolveLoginRequest(request);

        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password(), null);

        return authenticationManager.authenticate(authToken);
    }

    private LoginRequest resolveLoginRequest(HttpServletRequest request) {
        String contentType = request.getContentType();

        if (contentType != null && contentType.contains(MediaType.APPLICATION_JSON_VALUE)) {
            try {
                return objectMapper.readValue(request.getInputStream(), LoginRequest.class);
            } catch (IOException e) {
                throw new AuthenticationServiceException("Invalid login request body", e);
            }
        }

        return new LoginRequest(obtainUsername(request), obtainPassword(request));
    }

    @Override
    protected void successfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain,
            Authentication authentication
    ) throws IOException, ServletException {
        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Long userId = customUserDetails.getUserId();
        String email = customUserDetails.getUsername();
        String role = authentication.getAuthorities()
                .stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse("ROLE_USER");

        String token = jwtUtil.createJwt(userId, email, role, TOKEN_EXPIRE_MS);

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("email", email);
        data.put("role", role);
        data.put("token", token);

        response.addHeader("Authorization", "Bearer " + token);
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.success(data));
    }

    @Override
    protected void unsuccessfulAuthentication(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException failed
    ) throws IOException, ServletException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        objectMapper.writeValue(response.getWriter(), ApiResponse.error("LOGIN_FAILED"));
    }

    private record LoginRequest(String email, String password) {
    }
}
