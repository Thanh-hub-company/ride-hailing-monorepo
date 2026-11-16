package com.example.identityservice.domain.entity;

import com.example.commoncontracts.enums.ServiceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "vehicles",
        indexes = {
                @Index(name = "idx_vehicle_license_plate", columnList = "license_plate", unique = true),
                @Index(name = "idx_vehicle_driver_id", columnList = "driver_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vehicle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id", nullable = false)
    private DriverProfile driver;

    @Column(name = "make", nullable = false, length = 50)
    private String make; // Ví dụ: Honda, Yamaha, Toyota

    @Column(name = "model", nullable = false, length = 50)
    private String model; // Ví dụ: Wave Alpha, SH, Vios, City

    @Column(name = "color", nullable = false, length = 30)
    private String color; // Ví dụ: Đen, Trắng, Đỏ

    @Column(name = "license_plate", nullable = false, unique = true, length = 20)
    private String licensePlate; // Ví dụ: 29A-123.45

    @Column(name = "seat_capacity", nullable = false)
    private Integer seatCapacity; // Số chỗ ngồi (VD: 2 cho xe máy, 4 hoặc 7 cho ô tô)

    @Enumerated(EnumType.STRING)
    @Column(name = "service_type", nullable = false, length = 30)
    private ServiceType serviceType; // BIKE, CAR_4_SEAT, CAR_7_SEAT, DELIVERY

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = false; // Đang là xe chạy thực tế của tài xế hay không

    @Column(name = "is_verified", nullable = false)
    @Builder.Default
    private Boolean isVerified = false; // Đã được admin duyệt cà vẹt/đăng ký xe chưa

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}