# Bảng đối chiếu truy vết (Traceability Matrix)

| Mã yêu cầu | File/Class cài đặt | Test Case |
| :--- | :--- | :--- |
| FR-LOC-004, 006 | `TelemetryService.java` | `TelemetryServiceTest.java` (out-of-order) |
| CON-06 | `TripDispatchService.java` | `TripConcurrencyTest.java` (Testcontainers) |
| FR-PRI-016 | `PricingService.java` | `PricingServiceTest.java` (BigDecimal) |
| FR-RT-002 | `WebSocketGatewayHandler.java` | `WebSocketGatewayTest.java` |

### Các yêu cầu chưa phủ hoàn toàn (Follow-up)
- **Thanh toán thực tế**: Hiện tại đang dùng Mock/Sandbox adapter (theo yêu cầu).
- **Phân quyền nâng cao**: Đang dùng RBAC cơ bản, cần chi tiết hóa theo cấu trúc của `user-driver-service` trong các giai đoạn sau.
- **Monitoring**: Chưa tích hợp (đúng với phạm vi không làm K8s/Monitoring).
