package com.CheckMate.checkmate_server.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class UserDto {
    Long userId;
    String email;
    String password;
    String role;
}
