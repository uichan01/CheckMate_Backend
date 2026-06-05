package com.CheckMate.checkmate_server.security.service;

import com.CheckMate.checkmate_server.security.jwt.JWTFilter;
import com.CheckMate.checkmate_server.security.jwt.JWTUtil;
import com.CheckMate.checkmate_server.security.jwt.LoginFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JWTUtil jwtUtil;

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {

        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {

        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        //비활성화
        http.csrf((auth)->auth.disable());
        http.formLogin((auth)->auth.disable());
        http.httpBasic((auth)->auth.disable());

        // H2 콘솔 iframe 허용
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        //경로별 인가
        http
                .authorizeHttpRequests((auth) -> auth
                        .requestMatchers("/member/sign-up", "/", "/login","/h2-console/**",
                                "/swagger-ui/**", "/v3/api-docs/**").permitAll() //모두 허용
                        .requestMatchers("/admin/**").hasRole("ADMIN") //admin 만
                        .anyRequest().authenticated()); //인증된 사용자만

        //jwt 필터
        http
                .addFilterBefore(new JWTFilter(jwtUtil), LoginFilter.class);
        //로그인 필터
        http
                .addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil), UsernamePasswordAuthenticationFilter.class);

        //세션 stateless 로
        http
                .sessionManagement((session) -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }
}
