package com.ecommerce.global.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

/**
 * API 공통 응답 형식
 * 
 * 모든 API는 이 형식으로 응답합니다.
 * 
 * 성공 예시:
 * {
 *   "success": true,
 *   "message": "성공",
 *   "data": { "id": 1, "name": "홍길동" }
 * }
 * 
 * 실패 예시:
 * {
 *   "success": false,
 *   "message": "사용자를 찾을 수 없습니다",
 *   "data": null
 * }
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)  // null인 필드는 JSON에 포함 안 함
public class ApiResponse<T> {
    
    private final boolean success;  // 성공 여부
    private final String message;   // 메시지
    private final T data;          // 실제 데이터
    
    // 생성자는 private - 외부에서 직접 생성 못하게
    private ApiResponse(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }
    
    // ===== 성공 응답 만들기 =====
    
    /**
     * 데이터와 함께 성공 응답
     * 
     * 사용 예시:
     * return ApiResponse.success("조회 성공", userResponse);
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }
    
    /**
     * 데이터 없이 성공 응답
     * 
     * 사용 예시:
     * return ApiResponse.success("삭제 성공");
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message, null);
    }
    
    /**
     * 기본 성공 메시지와 함께
     * 
     * 사용 예시:
     * return ApiResponse.success(userResponse);
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(true, "성공", data);
    }
    
    // ===== 실패 응답 만들기 =====
    
    /**
     * 실패 응답
     * 
     * 사용 예시:
     * return ApiResponse.error("사용자를 찾을 수 없습니다");
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }
    
    /**
     * 실패 응답 with 데이터
     * 
     * 사용 예시:
     * return ApiResponse.error("검증 실패", validationErrors);
     */
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}