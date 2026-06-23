package com.CheckMate.checkmate_server.user.account.service;

import com.CheckMate.checkmate_server.user.account.dto.req.UpdateMyInfoRequestDto;
import com.CheckMate.checkmate_server.user.account.dto.res.MyInfoResponseDto;
import com.CheckMate.checkmate_server.user.account.dto.res.UserInfoResponseDto;

public interface UserAccountService {
    MyInfoResponseDto getMyInfo(Long userId);
    UserInfoResponseDto getUserInfo(String nickname);
    void updateMyInfo(Long userId, UpdateMyInfoRequestDto request);
}
