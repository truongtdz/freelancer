package com.freelancer.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    // Common
    USER_NOT_FOUND(404, "User not found"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    VALIDATION_ERROR(400, "Validation error"),
    INTERNAL_ERROR(500, "Internal server error"),

    // Auth
    DUPLICATE_EMAIL(409, "Email already exists"),
    DUPLICATE_USERNAME(409, "Username already exists"),
    INVALID_CREDENTIALS(401, "Invalid credentials"),
    INVALID_TOKEN(401, "Invalid or expired token"),
    ACCOUNT_BANNED(403, "Account is banned"),
    ACCOUNT_INACTIVE(403, "Account is inactive"),

    // File
    INVALID_FILE_TYPE(415, "Định dạng file không hỗ trợ"),
    FILE_TOO_LARGE(413, "File vượt quá kích thước cho phép"),
    FILE_UPLOAD_FAILED(500, "File upload failed"),

    // Job
    JOB_NOT_FOUND(404, "Không tìm thấy job"),
    JOB_NOT_OWNED(403, "You are not the owner of this job"),
    JOB_INVALID_STATUS(400, "Invalid job status for this action"),
    CATEGORY_NOT_FOUND(404, "Không tìm thấy danh mục"),
    SKILL_NOT_FOUND(404, "Không tìm thấy kỹ năng"),

    // Application
    DUPLICATE_APPLICATION(409, "Bạn đã ứng tuyển job này"),
    APPLICATION_NOT_FOUND(404, "Không tìm thấy ứng tuyển"),

    // Contract
    CONTRACT_NOT_FOUND(404, "Contract not found"),
    CONTRACT_NOT_PARTICIPANT(403, "You are not a participant of this contract"),
    INVALID_STATUS_TRANSITION(400, "Invalid status transition"),
    MAX_ATTEMPTS_EXCEEDED(400, "Maximum number of attempts exceeded"),

    // Payment
    PAYMENT_FAILED(500, "Payment failed"),
    TRANSACTION_NOT_FOUND(404, "Transaction not found"),
    INVALID_SIGNATURE(400, "Chữ ký VNPay không hợp lệ"),
    CONTRACT_CANCELLED(400, "Hợp đồng đã bị huỷ"),

    // Submission / Dispute
    SUBMISSION_LIMIT_EXCEEDED(400, "Đã vượt giới hạn submit — vui lòng mở dispute"),
    DISPUTE_ALREADY_EXISTS(409, "Hợp đồng đã có tranh chấp"),
    DISPUTE_NOT_FOUND(404, "Không tìm thấy tranh chấp"),

    // Review
    DUPLICATE_REVIEW(409, "Bạn đã review hợp đồng này"),

    // Profile / Payment
    PROFILE_NOT_FOUND(404, "Không tìm thấy profile"),
    PAYOUT_ALREADY_EXISTS(400, "Hợp đồng này đã được payout"),
    CONTRACT_NOT_READY_PAYOUT(400, "Hợp đồng chưa sẵn sàng payout"),
    INVALID_PARTIAL_AMOUNT(400, "Số tiền partial không hợp lệ");

    private final int httpStatus;
    private final String defaultMessage;

    ErrorCode(int httpStatus, String defaultMessage) {
        this.httpStatus = httpStatus;
        this.defaultMessage = defaultMessage;
    }
}
