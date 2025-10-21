package prm.project.prm392backend.exceptions;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    USERNAME_EXISTS(400, "Username already exists", HttpStatus.CONFLICT),
    EMAIL_EXISTS(400, "Email already exists", HttpStatus.CONFLICT),
    AUTH_INVALID_CREDENTIALS(401, "Invalid username or password", HttpStatus.UNAUTHORIZED),
    VALIDATION_FAILED(422, "Validation failed", HttpStatus.UNPROCESSABLE_ENTITY),
    INTERNAL_ERROR(500, "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR),
    USER_NOT_FOUND(404, "User not found", HttpStatus.NOT_FOUND),
    PRODUCT_NOT_FOUND(404, "Product not found", HttpStatus.NOT_FOUND),
    CART_NOT_FOUND(404, "Cart not found", HttpStatus.NOT_FOUND),
    CART_ITEM_NOT_FOUND(404, "Cart item not found", HttpStatus.NOT_FOUND),
    QUANTITY_INVALID(422, "Quantity is invalid", HttpStatus.UNPROCESSABLE_ENTITY),
    MISSING_PARAMETER(400, "Missing required parameter", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_REQUIRED(400, "Category name is required", HttpStatus.BAD_REQUEST),
    CATEGORY_CONFLICT(409, "Category name already exists", HttpStatus.CONFLICT),
    CATEGORY_NOT_FOUND(404, "Category not found", HttpStatus.NOT_FOUND),
    CONVERSATION_ID_REQUIRED(400, "conversationId is required", HttpStatus.BAD_REQUEST),
    MESSAGE_REQUIRED(400, "message is required", HttpStatus.BAD_REQUEST),
    EXTERNAL_SERVICE_ERROR(502, "Upstream service error", HttpStatus.BAD_GATEWAY),

    AUTH_MISSING(401, "Missing Authorization: Bearer <token>", HttpStatus.UNAUTHORIZED),
    AUTH_INVALID(401, "Invalid or expired token", HttpStatus.UNAUTHORIZED),
    TOKEN_NO_USERID(401, "Token has no userId", HttpStatus.UNAUTHORIZED),
    ORDER_CREATE_FAILED(500, "Failed to create order", HttpStatus.INTERNAL_SERVER_ERROR),
    ORDER_NOT_FOUND(404, "Order not found", HttpStatus.NOT_FOUND),
    ORDER_INVALID_STATE(400, "Order is not in WAITING PAYMENT state", HttpStatus.BAD_REQUEST),
    PAYMENT_URL_CREATE_FAILED(500, "Failed to create payment URL", HttpStatus.INTERNAL_SERVER_ERROR),
    PAYMENT_UPDATE_FAILED(500, "Failed to update payment", HttpStatus.INTERNAL_SERVER_ERROR),

    PRODUCT_NAME_REQUIRED(400, "productName is required", HttpStatus.BAD_REQUEST),
    CATEGORY_ID_REQUIRED(400, "categoryId is required", HttpStatus.BAD_REQUEST),
    PRODUCT_CONFLICT(409, "Product already exists", HttpStatus.CONFLICT),

    PROVIDER_NOT_FOUND(404, "You are not the provider", HttpStatus.NOT_FOUND),

    PROVIDER_ALREADY_EXISTS(400, "You are already a supplier", HttpStatus.CONFLICT)
    ;

    ErrorCode(int code, String message, HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
        this.code = code;
        this.message = message;
    }

    private final int code;
    private final String message;
    private final HttpStatus httpStatus;

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }
}
