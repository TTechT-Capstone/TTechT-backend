package com.example.TTECHT.exception;


import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least {min} characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least {min} characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    INVALID_USER_ID(1009, "Invalid user id", HttpStatus.BAD_REQUEST),
    UPDATE_USER_FAILED(1010, "Update user failed", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_ALREADY_SELLER(1011, "User is already a seller", HttpStatus.BAD_REQUEST),
    SELLER_NOT_FOUND(1012, "Seller not found", HttpStatus.NOT_FOUND),
    STORE_NAME_ALREADY_EXISTS(1013, "Store name already exists", HttpStatus.BAD_REQUEST),
    SELLER_CREATION_FAILED(1014, "Failed to create seller", HttpStatus.INTERNAL_SERVER_ERROR),
    SELLER_UPDATE_FAILED(1013, "Failed to update seller", HttpStatus.INTERNAL_SERVER_ERROR),
    ;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

}
