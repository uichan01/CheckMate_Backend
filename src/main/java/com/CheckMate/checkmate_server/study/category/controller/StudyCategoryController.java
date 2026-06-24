package com.CheckMate.checkmate_server.study.category.controller;

import com.CheckMate.checkmate_server.response.ApiResponse;
import com.CheckMate.checkmate_server.study.category.dto.res.StudyCategoryResponseDto;
import com.CheckMate.checkmate_server.study.category.service.StudyCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/study/category")
public class StudyCategoryController {
    private final StudyCategoryService categoryService;

    // 카테고리 추가
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> addStudyCategory(@RequestParam String categoryName) {
        Long categoryId = categoryService.addStudyCategoryEntity(categoryName);
        return ResponseEntity.ok(ApiResponse.success(categoryId));
    }

    // 카테고리 전체 조회
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<StudyCategoryResponseDto>>> getCategories() {
        List<StudyCategoryResponseDto> categories = categoryService.getCategories();
        return ResponseEntity.ok(ApiResponse.success(categories));
    }
}
