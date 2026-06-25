package com.CheckMate.checkmate_server.user.auth.service;

import com.CheckMate.checkmate_server.user.auth.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.auth.dto.res.SignUpResponseDto;

public interface UserAuthService {
    public SignUpResponseDto signUp(SignUpRequestDto request);
    public boolean checkEmailDuplication(String email);
    //    public void deleteUser(String email);
    public void deleteUser(Long userId);
}
