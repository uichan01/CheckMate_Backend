package com.CheckMate.checkmate_server.user.dto.res;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SignUpResponseDto {

    private Long userId;
    private String email;
    private String nickname;
    private String role;
    private String token;
}
