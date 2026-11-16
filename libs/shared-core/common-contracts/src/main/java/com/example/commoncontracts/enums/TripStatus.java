package com.example.commoncontracts.enums;

public enum TripStatus {
    CREATED,    // Khởi tạo yêu cầu
    MATCHING,   // Đang tìm tài xế
    ACCEPTED,   // Tài xế nhận chuyến
    PICKING_UP, // Đang tới điểm đón
    ARRIVED,    // Đã tới điểm đón
    IN_TRIP,    // Đang trong hành trình
    COMPLETED,  // Chuyến hoàn tất
    CANCELLED,  // Chuyến bị hủy
    NO_DRIVER   // Không tìm thấy tài xế
}