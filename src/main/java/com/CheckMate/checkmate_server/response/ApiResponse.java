package com.CheckMate.checkmate_server.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private boolean success;
    private int code;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, 200, "OK", data);
    }

    public static ApiResponse<Void> success() {
        return new ApiResponse<>(true, 200, "OK", null);
    }

    public static ApiResponse<Void> error(int code, String message) {
        return new ApiResponse<>(false, code, message, null);
    }
}
