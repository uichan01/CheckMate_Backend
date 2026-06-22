package com.CheckMate.checkmate_server.study.dto;

import com.CheckMate.checkmate_server.user.domain.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleUserDto {
    private long userId;
    private String nickName;

    public static SimpleUserDto from(UserEntity userEntity) {
        return SimpleUserDto.builder()
                .userId(userEntity.getUserId())
                .nickName(userEntity.getNickname())
                .build();
    }
}
