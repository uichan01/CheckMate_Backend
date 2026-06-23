package com.CheckMate.checkmate_server.user.account.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.security.dto.CustomUserDetails;
import com.CheckMate.checkmate_server.user.account.dto.req.UpdateMyInfoRequestDto;
import com.CheckMate.checkmate_server.user.account.dto.res.MyInfoResponseDto;
import com.CheckMate.checkmate_server.user.account.dto.res.UserInfoResponseDto;
import com.CheckMate.checkmate_server.user.account.service.UserAccountServiceImpl;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
public class UserAccountController {

    private final UserAccountServiceImpl userAccountService;

    //내 정보 조회
    @GetMapping("/account")
    public ResponseEntity<ApiResponse<MyInfoResponseDto>> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        MyInfoResponseDto response = userAccountService.getMyInfo(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //타인 정보 조회
    @GetMapping("/info/{nickname}")
    public ResponseEntity<ApiResponse<UserInfoResponseDto>> getUserInfo(@PathVariable String nickname) {
        UserInfoResponseDto response = userAccountService.getUserInfo(nickname);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    //내 정보 수정
    @PatchMapping("/info")
    public ResponseEntity<ApiResponse<Void>> updateMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateMyInfoRequestDto request
    ) {
        userAccountService.updateMyInfo(userDetails.getUsername(), request);
        return ResponseEntity.ok(ApiResponse.success());
    }
}
