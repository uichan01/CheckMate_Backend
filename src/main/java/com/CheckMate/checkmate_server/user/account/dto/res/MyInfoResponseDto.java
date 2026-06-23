package com.CheckMate.checkmate_server.user.account.dto.res;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MyInfoResponseDto {

    private long userId;
    private String nickname;
    private String intro;
    private String role;
    private List<MyStudyDto> studies;
}
