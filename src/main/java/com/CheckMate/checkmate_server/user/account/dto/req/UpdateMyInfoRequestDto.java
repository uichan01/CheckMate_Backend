package com.CheckMate.checkmate_server.user.account.dto.req;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMyInfoRequestDto {

    @Size(min = 8, message = "비밀번호는 8자 이상이어야 합니다.")
    private String password;

    @Size(max = 20, message = "닉네임은 20자 이하여야 합니다.")
    private String nickname;

    @Size(max = 255, message = "소개글은 255자 이하여야 합니다.")
    private String intro;
}
