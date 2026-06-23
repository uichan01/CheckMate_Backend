package com.CheckMate.checkmate_server.user.auth.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.user.auth.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.auth.dto.res.SignUpResponseDto;
import com.CheckMate.checkmate_server.user.auth.service.UserAuthServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user/auth")
public class UserAuthController {

    private final UserAuthServiceImpl userService;

    //회원가입
    @PostMapping("/sign-up")
    public ApiResponse<SignUpResponseDto> signUp(@Valid @RequestBody SignUpRequestDto request) {
        SignUpResponseDto response = userService.signUp(request);
        return ApiResponse.success(response);
    }

    //로그인 유저 본인 회원탈퇴
    @DeleteMapping("/me")
    public ApiResponse<Void> deleteUser(@AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(userDetails.getUsername());
        return ApiResponse.success();
    }

    //이메일 중복검사
    @GetMapping("/check-email")
    public ApiResponse<Boolean> checkEmailDuplication(@RequestParam String email) {
        boolean result = userService.checkEmailDuplication(email);
        return ApiResponse.success(result);
    }

    //로그아웃
}
