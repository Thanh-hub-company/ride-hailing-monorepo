package com.example.commoncontracts.enums;

public enum OfferStatus {
    PENDING,   // Đã gửi offer tới tài xế
    ACCEPTED,  // Tài xế chấp nhận
    REJECTED,  // Tài xế từ chối
    EXPIRED    // Hết thời hạn phản hồi (15s)
}