package com.CheckMate.checkmate_server.user.account.service;

import com.CheckMate.checkmate_server.user.account.dto.req.UpdateMyInfoRequestDto;
import com.CheckMate.checkmate_server.user.account.dto.res.MyInfoResponseDto;
import com.CheckMate.checkmate_server.user.account.dto.res.UserInfoResponseDto;

public interface UserAccountService {
    public MyInfoResponseDto getMyInfo(String email);
    public UserInfoResponseDto getUserInfo(String nickname);
    public void updateMyInfo(String email, UpdateMyInfoRequestDto request);
}
