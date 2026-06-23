package com.CheckMate.checkmate_server.user.auth.service;

import com.CheckMate.checkmate_server.user.auth.dto.req.SignUpRequestDto;
import com.CheckMate.checkmate_server.user.auth.dto.res.SignUpResponseDto;

public interface UserAuthService {
    public SignUpResponseDto signUp(SignUpRequestDto request);
    public void deleteUser(String email);
    public boolean checkEmailDuplication(String email);
}
