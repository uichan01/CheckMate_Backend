package com.CheckMate.checkmate_server.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Builder
@Getter
public class MemberDto {
    String email;
    String password;
    String role;
}
