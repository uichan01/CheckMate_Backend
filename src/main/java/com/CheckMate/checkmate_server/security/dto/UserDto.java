package com.CheckMate.checkmate_server.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class UserDto {
    String email;
    String password;
    String role;
}
