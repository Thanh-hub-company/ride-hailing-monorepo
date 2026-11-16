# 🚗 Ride-Hailing Microservices Platform

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.3.x-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Cloud Gateway](https://img.shields.io/badge/Spring_Cloud-Gateway-0078D7?style=for-the-badge&logo=spring&logoColor=white)](https://spring.io/projects/spring-cloud-gateway)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-GEO_6.x+-DC382D?style=for-the-badge&logo=redis&logoColor=white)](https://redis.io/)
[![Architecture](https://img.shields.io/badge/Architecture-Microservices_Monorepo-0A84FF?style=for-the-badge)](#-1-kiến-trúc-microservices--danh-sách-port)

> **Hệ thống đặt xe công nghệ (Ride-Hailing)** được thiết kế và triển khai theo kiến trúc **Microservices Monorepo** hiện đại trên nền tảng **Java 21** và **Spring Boot 3**. Hệ thống xử lý toàn diện chu trình gọi xe theo thời gian thực: Xác thực Stateless JWT, Định vị GPS tài xế thời gian thực với Redis GEO, Tính cước động (Surge Pricing), Điều phối khớp tài xế - chuyến xe (Trip State Machine), Xử lý thanh toán Idempotent và Sổ cái ví tài xế (Double-Entry Ledger).

---

## 📑 Mục Lục

- [1. Kiến trúc Microservices & Danh sách Port](#-1-kiến-trúc-microservices--danh-sách-port)
- [2. Sơ đồ Luồng Hoạt Động & Vòng đời Chuyến xe (State Machine)](#-2-sơ-đồ-luồng-hoạt-động--vòng-đời-chuyến-xe-state-machine)
  - [2.1. Luồng tổng thể hệ thống (End-to-End Workflow)](#21-luồng-tổng-thể-hệ-thống-end-to-end-workflow)
  - [2.2. Vòng đời trạng thái chuyến đi (Trip Lifecycle)](#22-vòng-đời-trạng-thái-chuyến-đi-trip-lifecycle)
- [3. Cấu trúc Cơ sở Dữ liệu & Dữ liệu Mẫu (PostgreSQL)](#-3-cấu-trúc-cơ-sở-dữ-liệu--dữ-liệu-mẫu-postgresql)
  - [3.1. Database `identity_db`](#31-database-identity_db)
  - [3.2. Database `pricing_db`](#32-database-pricing_db)
  - [3.3. Database `trip_db`](#33-database-trip_db)
  - [3.4. Database `wallet_db`](#34-database-wallet_db)
- [4. Khởi tạo Hạ tầng Redis (Telemetry Service)](#-4-khởi-tạo-hạ-tầng-redis-telemetry-service)
- [5. Hướng dẫn Kiểm thử API Từng Bước (API Testing Workflow)](#-5-hướng-dẫn-kiểm-thử-api-từng-bước-api-testing-workflow)
  - [Bước 1: Đăng nhập Khách hàng & Tài xế](#-bước-1-đăng-nhập--lấy-jwt-token)
  - [Bước 2: Bật trạng thái sẵn sàng cho Tài xế](#-bước-2-tài-xế-bật-trạng-thái-sẵn-sàng-available)
  - [Bước 3: Khách hàng yêu cầu Báo giá cước](#-bước-3-khách-hàng-yêu-cầu-báo-giá-cước-pricing)
  - [Bước 4: Khách hàng Tạo chuyến đi mới](#-bước-4-khách-hàng-tạo-chuyến-đi-create-trip)
  - [Bước 5: Tài xế Nhận & Chấp nhận đề nghị cuốc](#-bước-5-tài-xế-nhận-offer--chấp-nhận-cuốc-xe)
  - [Bước 6: Tài xế Cập nhật trạng thái di chuyển](#-bước-6-tài-xế-cập-nhật-các-trạng-thái-chuyến-đi)
  - [Bước 7: Thanh toán cước phí & Phân bổ ví tài xế](#-bước-7-khách-hàng-thanh-toán-chuyến-đi-payment)
  - [Bước 8: Kiểm tra Sổ cái & Biến động số dư ví](#-bước-8-tài-xế-kiểm-tra-sổ-cái--số-dư-ví)
- [6. Bảng Tra Cứu Lỗi Thường Gặp (Troubleshooting FAQ)](#-6-bảng-tra-cứu-lỗi-thường-gặp-troubleshooting-faq)
- [7. Danh sách Tài khoản Kiểm thử Mặc định](#-7-danh-sách-tài-khoản-kiểm-thử-mặc-định)
- [8. Cấu trúc Thư mục Dự án (Project Structure)](#-8-cấu-trúc-thư-mục-dự-án-project-structure)

---

## 🏛️ 1. Kiến trúc Microservices & Danh sách Port

Hệ thống cho phép gọi API qua **API Gateway tập trung** (khuyên dùng) hoặc kết nối trực tiếp đến từng Service:

| Module / Service | Thư mục | Port Gateway | Port Trực tiếp | Lưu trữ / Database | Chức năng cốt lõi |
| :--- | :--- | :---: | :---: | :--- | :--- |
| 🛡️ **`api-gateway`** | `apps/api-gateway` | **`8081`** | — | — | Routing tập trung, Whitelist bảo mật, Reverse Proxy, Forward Headers |
| 🔑 **`user-driver-service`** | `apps/user-driver-service` | `8081` | **`8085`** | PostgreSQL (`identity_db`) | Quản lý User/Driver, Đăng ký/Đăng nhập, Cấp phát JWT & Trạng thái tài xế |
| 💵 **`pricing-service`** | `apps/pricing-service` | `8081` | **`8084`** | PostgreSQL (`pricing_db`) | Tính toán cước phí cơ bản, hệ số Surge động theo khoảng cách & khu vực |
| 📡 **`location-service`** | `apps/location-service` | `8081` | **`8083`** | Redis (`localhost:6379`) | Thu thập tọa độ GPS thời gian thực, quét tài xế bán kính lân cận (GEO) |
| 🚖 **`trip-dispatch-service`** | `apps/trip-dispatch-service` | `8081` | **`8086`** | PostgreSQL (`trip_db`) | Quản lý vòng đời chuyến đi, phát Offer cho tài xế, điều phối chuyến |
| 💳 **`payment-wallet-service`** | `apps/payment-wallet-service` | `8081` | **`8082`** | PostgreSQL (`wallet_db`) | Xử lý thanh toán Idempotent, quản lý số dư Ví, bút toán Sổ cái (Ledger) |

---

## 🔄 2. Sơ đồ Luồng Hoạt Động & Vòng đời Chuyến xe (State Machine)

### 2.1. Luồng tổng thể hệ thống (End-to-End Workflow)

```
                            ┌─────────────────────────┐
                            │   Khách hàng / Tài xế   │
                            └────────────┬────────────┘
                                         │ (Request qua API Gateway :8081)
                                         ▼
┌─────────────────────────────────────────────────────────────────────────────────┐
│ 1. XÁC THỰC (user-driver-service :8085)                                          │
│    POST /api/v1/auth/login ➔ Trả về Stateless JWT Token (Bearer Header)         │
└────────────────────────┬────────────────────────────────────────────────────────┘
                         │
      ┌──────────────────┴───────────────────┐
      ▼                                      ▼
┌──────────────────────────────────┐   ┌──────────────────────────────────────────┐
│ 2. ĐỊNH VỊ GPS TÀI XẾ            │   │ 3. TÍNH BÁO GIÁ CƯỚC                     │
│    (location-service :8083)      │   │    (pricing-service :8084)               │
│    Gửi tọa độ GPS định kỳ        │   │    Khách nhập điểm đón & trả             │
│    ➔ Cập nhật Redis GEO          │   │    ➔ Trả về Quote ID & Cước tạm tính     │
└──────────────────────────────────┘   └────────────────────┬─────────────────────┘
                                                            │
                                                            ▼
                                       ┌──────────────────────────────────────────┐
                                       │ 4. TẠO CHUYẾN ĐI & KHỚP TÀI XẾ           │
                                       │    (trip-dispatch-service :8086)         │
                                       │    Khách tạo chuyến (MATCHING)           │
                                       │    ➔ Quét tìm tài xế AVAILABLE lân cận   │
                                       │    ➔ Tự động phát Driver Offer           │
                                       └────────────────────┬─────────────────────┘
                                                            │
                                                            ▼
                                       ┌──────────────────────────────────────────┐
                                       │ 5. ĐIỀU PHỐI & THỰC HIỆN CHUYẾN ĐI       │
                                       │    Tài xế chấp nhận Offer (ACCEPTED)     │
                                       │    ➔ Đón khách (PICKING_UP)              │
                                       │    ➔ Đã đến điểm đón (ARRIVED)           │
                                       │    ➔ Bắt đầu di chuyển (IN_TRIP)         │
                                       │    ➔ Trả khách thành công (COMPLETED)    │
                                       └────────────────────┬─────────────────────┘
                                                            │
                                                            ▼
                                       ┌──────────────────────────────────────────┐
                                       │ 6. THANH TOÁN & GHI SỔ CÁI (LEDGER)      │
                                       │    (payment-wallet-service :8082)        │
                                       │    Khách xác nhận thanh toán             │
                                       │    ➔ Cộng cước chuyến vào ví (+100%)     │
                                       │    ➔ Khấu trừ hoa hồng sàn (-20%)        │
                                       └──────────────────────────────────────────┘
```

### 2.2. Vòng đời trạng thái chuyến đi (Trip Lifecycle)

```
 [MATCHING] ──(Tài xế Accept)──► [ACCEPTED] ──(Đi đón)──► [PICKING_UP]
     │                                                         │
     │                                                         ▼
     │                                                    [ARRIVED] (Đã tới điểm đón)
     │                                                         │
     │                                                         ▼
     │                                                    [IN_TRIP] (Bắt đầu chuyến)
     │                                                         │
     │                                                         ▼
     └──(Hủy cuốc)──► [CANCELLED] ◄──────(Hủy cuốc)────── [COMPLETED] ──► [THANH TOÁN]
```

---

## 🗄️ 3. Cấu trúc Cơ sở Dữ liệu & Dữ liệu Mẫu (PostgreSQL)

> 💡 **Hướng dẫn chạy**: Mở cửa sổ **Database Console** trong IntelliJ IDEA hoặc DBeaver, kết nối đến PostgreSQL (`localhost:5432`) và chạy script tương ứng vào từng Database.

### 3.1. Database `identity_db`

```sql
DROP TABLE IF EXISTS driver_profiles CASCADE;
DROP TABLE IF EXISTS refresh_tokens CASCADE;
DROP TABLE IF EXISTS users CASCADE;

CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) UNIQUE NOT NULL,
    phone_number VARCHAR(20) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE driver_profiles (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    vehicle_license_plate VARCHAR(50) NOT NULL,
    vehicle_model VARCHAR(100) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    rating DOUBLE PRECISION NOT NULL DEFAULT 5.0,
    is_approved BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token VARCHAR(1000) NOT NULL UNIQUE,
    expiry_date TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE
);

CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone_number);

-- Dữ liệu mẫu (Mật khẩu mặc định: Password123!)
INSERT INTO users (id, email, phone_number, password_hash, full_name, role) VALUES
    ('11111111-1111-1111-1111-111111111101', 'customer1@example.com', '0901234567', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Nguyen Van Khach', 'CUSTOMER'),
    ('11111111-1111-1111-1111-111111111102', 'customer2@example.com', '0902222222', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Tran Thi B', 'CUSTOMER'),
    ('22222222-2222-2222-2222-222222222201', 'driver1@example.com', '0987654321', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Tran Van Taixe', 'DRIVER'),
    ('22222222-2222-2222-2222-222222222202', 'driver2@example.com', '0988888888', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'Le Van Tai', 'DRIVER'),
    ('33333333-3333-3333-3333-333333333301', 'admin@example.com', '0999999999', '$2a$10$Y7f8tqDsWQ22ya6bAn42MONFXKPZROXk0VKf6YL1dh2AIfoWrwfyC', 'System Admin', 'ADMIN')
ON CONFLICT (id) DO NOTHING;

INSERT INTO driver_profiles (id, user_id, vehicle_license_plate, vehicle_model, status, rating, is_approved) VALUES
    ('22222222-2222-2222-2222-222222222211', '22222222-2222-2222-2222-222222222201', '51A-123.45', 'Honda Wave Alpha', 'AVAILABLE', 5.0, TRUE),
    ('22222222-2222-2222-2222-222222222212', '22222222-2222-2222-2222-222222222202', '51B-678.90', 'Toyota Vios', 'AVAILABLE', 4.8, TRUE)
ON CONFLICT (id) DO NOTHING;
```

---

### 3.2. Database `pricing_db`

```sql
DROP TABLE IF EXISTS fare_quotes CASCADE;

CREATE TABLE fare_quotes (
    id VARCHAR(36) PRIMARY KEY,
    service_type VARCHAR(30) NOT NULL,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    distance_km DOUBLE PRECISION NOT NULL,
    total_fare NUMERIC(12,2) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);
```

---

### 3.3. Database `trip_db`

```sql
DROP TABLE IF EXISTS driver_offers CASCADE;
DROP TABLE IF EXISTS trips CASCADE;

CREATE TABLE trips (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_code VARCHAR(50) UNIQUE,
    customer_id UUID NOT NULL,
    driver_id UUID,
    quote_id VARCHAR(36),
    service_type VARCHAR(30) NOT NULL,
    pickup_latitude DOUBLE PRECISION NOT NULL,
    pickup_longitude DOUBLE PRECISION NOT NULL,
    dropoff_latitude DOUBLE PRECISION NOT NULL,
    dropoff_longitude DOUBLE PRECISION NOT NULL,
    fare_amount NUMERIC(12,2) NOT NULL,
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH',
    status VARCHAR(30) NOT NULL DEFAULT 'MATCHING',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP WITH TIME ZONE
);

CREATE TABLE driver_offers (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL REFERENCES trips(id) ON DELETE CASCADE,
    driver_id UUID NOT NULL,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    offered_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_trips_customer_id ON trips(customer_id);
CREATE INDEX idx_trips_driver_id ON trips(driver_id);
CREATE INDEX idx_driver_offers_driver_id ON driver_offers(driver_id);
CREATE INDEX idx_driver_offers_trip_id ON driver_offers(trip_id);
```

---

### 3.4. Database `wallet_db`

```sql
DROP TABLE IF EXISTS payments CASCADE;
DROP TABLE IF EXISTS wallet_entries CASCADE;
DROP TABLE IF EXISTS wallets CASCADE;

CREATE TABLE wallets (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    driver_id UUID NOT NULL UNIQUE,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE wallet_entries (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    wallet_id UUID NOT NULL REFERENCES wallets(id) ON DELETE CASCADE,
    trip_id UUID,
    type VARCHAR(50) NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    reference_code VARCHAR(100) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE payments (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    trip_id UUID NOT NULL,
    customer_id UUID NOT NULL,
    amount NUMERIC(12,2) NOT NULL,
    currency VARCHAR(10) NOT NULL DEFAULT 'VND',
    payment_method VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    provider_ref VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_wallets_driver_id ON wallets(driver_id);
CREATE INDEX idx_entries_wallet_id ON wallet_entries(wallet_id);
CREATE INDEX idx_entries_trip_id ON wallet_entries(trip_id);
CREATE INDEX idx_payments_trip_id ON payments(trip_id);
CREATE INDEX idx_payments_customer_id ON payments(customer_id);

-- Khởi tạo ví mẫu cho tài xế
INSERT INTO wallets (id, driver_id, currency) VALUES
    ('55555555-5555-5555-5555-555555555501', '22222222-2222-2222-2222-222222222201', 'VND'),
    ('55555555-5555-5555-5555-555555555502', '22222222-2222-2222-2222-222222222202', 'VND')
ON CONFLICT (driver_id) DO NOTHING;

-- Khởi tạo số dư ban đầu
INSERT INTO wallet_entries (id, wallet_id, type, amount, reference_code) VALUES
    ('66666666-6666-6666-6666-666666666601', '55555555-5555-5555-5555-555555555501', 'ADJUSTMENT', 500000.00, 'INITIAL_DEPOSIT'),
    ('66666666-6666-6666-6666-666666666602', '55555555-5555-5555-5555-555555555502', 'ADJUSTMENT', 300000.00, 'INITIAL_DEPOSIT')
ON CONFLICT (id) DO NOTHING;
```

---

## ⚡ 4. Khởi tạo Hạ tầng Redis (Telemetry Service)

Đảm bảo Redis Server đang lắng nghe trên cổng `localhost:6379`. Nạp sẵn tọa độ định vị mẫu cho 2 tài xế tại khu vực TP.HCM bằng `redis-cli`:

```bash
# Cú pháp: GEOADD <key> <longitude> <latitude> <member>
redis-cli GEOADD driver:locations 106.7009 10.7769 22222222-2222-2222-2222-222222222201
redis-cli GEOADD driver:locations 106.7050 10.7800 22222222-2222-2222-2222-222222222202
```

---

## 🚀 5. Hướng dẫn Kiểm thử API Từng Bước (API Testing Workflow)

> 💡 **Khuyến nghị**: Sử dụng biến môi trường Postman `{{baseUrl}} = http://localhost:8081` để gọi toàn bộ API qua API Gateway tập trung.

---

### 🟢 Bước 1: Đăng nhập & Lấy JWT Token

#### 1.1. Khách hàng Đăng nhập (Customer)
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "email": "customer1@example.com",
  "password": "Password123!"
}
```
👉 *Lưu `accessToken` vào biến `{{customerToken}}`.*

#### 1.2. Tài xế Đăng nhập (Driver)
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/auth/login`
- **Headers**: `Content-Type: application/json`
- **Body**:
```json
{
  "email": "driver1@example.com",
  "password": "Password123!"
}
```
👉 *Lưu `accessToken` vào biến `{{driverToken}}`.*

---

### 🟢 Bước 2: Tài xế bật trạng thái sẵn sàng (AVAILABLE)

- **Method**: `PUT`
- **URL**: `http://localhost:8081/api/v1/drivers/me/status`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {{driverToken}}`
- **Body**:
```json
{
  "status": "AVAILABLE"
}
```

---

### 🟢 Bước 3: Khách hàng yêu cầu Báo giá cước (Pricing)

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/quotes` *(hoặc `http://localhost:8081/api/v1/pricing/quote`)*
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {{customerToken}}`
- **Body**:
```json
{
  "serviceType": "BIKE",
  "pickupLatitude": 10.7769,
  "pickupLongitude": 106.7009,
  "dropoffLatitude": 10.7825,
  "dropoffLongitude": 106.6983
}
```
👉 *Lưu `id` trả về vào biến `{{quoteId}}` và giá cước `amount`.*

---

### 🟢 Bước 4: Khách hàng Tạo chuyến đi (Create Trip)

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/trips`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {{customerToken}}`
  - `Idempotency-Key`: `{{$guid}}` *(hoặc chuỗi ngẫu nhiên mới: `trip-key-01`)*
- **Body**:
```json
{
  "quoteId": "{{quoteId}}",
  "serviceType": "BIKE",
  "pickupLatitude": 10.7769,
  "pickupLongitude": 106.7009,
  "dropoffLatitude": 10.7825,
  "dropoffLongitude": 106.6983,
  "fareAmount": 25000.0,
  "paymentMethod": "CASH"
}
```
👉 *Hệ thống trả về `201 Created` và chuyến đi ở trạng thái `MATCHING`. Lưu `id` vào biến `{{tripId}}`.*

---

### 🟢 Bước 5: Tài xế Nhận Offer & Chấp nhận cuốc xe

#### 5.1. Tài xế lấy danh sách đề nghị chuyến đang chờ (Pending Offers)
- **Method**: `GET`
- **URL**: `http://localhost:8081/api/v1/trips/driver/offers/pending`
- **Headers**: `Authorization: Bearer {{driverToken}}`
👉 *Lấy trường `id` của Offer và gán vào biến `{{offerId}}`.*

#### 5.2. Tài xế Chấp nhận cuốc (Accept Offer)
- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/trips/driver/offers/{{offerId}}/accept`
- **Headers**: `Authorization: Bearer {{driverToken}}`
- **Body**: Để trống (None)
👉 *Chuyến xe chuyển sang trạng thái `ACCEPTED`.*

---

### 🟢 Bước 6: Tài xế Cập nhật các trạng thái chuyến đi

Dùng cùng API `PUT http://localhost:8081/api/v1/trips/{{tripId}}/status` kèm Header `Authorization: Bearer {{driverToken}}` và `Content-Type: application/json` theo thứ tự:

#### 6.1. Đang đi đón khách (`PICKING_UP`)
```json
{
  "targetStatus": "PICKING_UP"
}
```

#### 6.2. Đã tới điểm đón (`ARRIVED`)
```json
{
  "targetStatus": "ARRIVED"
}
```

#### 6.3. Khách đã lên xe, bắt đầu di chuyển (`IN_TRIP`)
```json
{
  "targetStatus": "IN_TRIP"
}
```

#### 6.4. Đã trả khách, hoàn tất chuyến (`COMPLETED`)
```json
{
  "targetStatus": "COMPLETED"
}
```

---

### 🟢 Bước 7: Khách hàng Thanh toán chuyến đi (Payment)

- **Method**: `POST`
- **URL**: `http://localhost:8081/api/v1/payments/process`
- **Headers**:
  - `Content-Type`: `application/json`
  - `Authorization`: `Bearer {{customerToken}}`
- **Body**:
```json
{
  "tripId": "{{tripId}}"
}
```
*(Hệ thống tự động tra cứu cước phí chuyến đi, ghi nhận thanh toán `SUCCEEDED` và tự động phân bổ thu nhập vào ví tài xế).*

---

### 🟢 Bước 8: Tài xế kiểm tra Sổ cái & Số dư ví

#### 8.1. Kiểm tra biến động số dư theo chuyến đi (Trip Wallet Entries)
- **Method**: `GET`
- **URL**: `http://localhost:8081/api/v1/driver/wallet/entries?tripId={{tripId}}`
- **Headers**: `Authorization: Bearer {{driverToken}}`
👉 *Trả về 2 bút toán: Cộng cước `TRIP_EARNING` (+25,000 VND) và Trừ chiết khấu `PLATFORM_COMMISSION` (-5,000 VND).*

#### 8.2. Kiểm tra tổng số dư ví hiện tại (Wallet Balance)
- **Method**: `GET`
- **URL**: `http://localhost:8081/api/v1/wallets/balance`
- **Headers**: `Authorization: Bearer {{driverToken}}`

---

## 🛠️ 6. Bảng Tra Cứu Lỗi Thường Gặp (Troubleshooting FAQ)

| Mã lỗi / Hiện tượng | Nguyên nhân | Hướng dẫn khắc phục |
| :--- | :--- | :--- |
| `IDEMPOTENCY_KEY_REUSED` | Gửi request tạo chuyến mới với `Idempotency-Key` cũ đã từng dùng trước đó | Đổi giá trị header `Idempotency-Key` sang mã mới (dùng `{{$guid}}` trong Postman) |
| `QUOTE_EXPIRED` | Mã báo giá (`quoteId`) đã hết hạn hiệu lực | Gọi lại API `POST /api/v1/quotes` để tạo quote mới và tạo chuyến ngay sau đó |
| `Offer đã hết hiệu lực` | Tài xế bấm Accept sau khi Offer hết hạn | Hệ thống đã tăng hạn lên 24h. Đảm bảo đã Restart `trip-dispatch-service` |
| `Không thể chuyển trạng thái từ MATCHING sang PICKING_UP` | Tài xế chưa gọi API Accept Offer mà đã nhảy sang bước đi đón | Gọi `POST /api/v1/trips/driver/offers/{{offerId}}/accept` trước khi đón khách |
| `Không thể chuyển trạng thái từ COMPLETED sang ...` | Chuyến đi đã kết thúc, không thể đảo ngược trạng thái | Tạo chuyến đi mới theo quy trình từ Bước 3 để test lại |
| `405 Method Not Allowed` khi đổi trạng thái | Dùng nhầm HTTP Method `PATCH` hoặc `POST` | Đổi HTTP Method sang **`PUT`** cho endpoint `/api/v1/trips/{tripId}}/status` |
| `404 Not Found` khi Accept Offer | Thiếu tiền tố `/trips` trong đường dẫn URL | Sử dụng đúng URL: `/api/v1/trips/driver/offers/{offerId}/accept` |
| `GET pending offers` trả về `[]` rỗng | Tài xế đang `OFFLINE` hoặc chưa được duyệt `is_approved` tại lúc tạo chuyến | Bật `AVAILABLE` cho tài xế trước khi khách hàng bấm tạo chuyến đi |

---

## 👥 7. Danh sách Tài khoản Kiểm thử Mặc định

*Tất cả tài khoản mẫu đều sử dụng mật khẩu:* **`Password123!`**

| Vai trò (Role) | Email đăng nhập | Quyền hạn & Chức năng | Số dư Ví khởi tạo |
| :--- | :--- | :--- | :---: |
| 🧑‍💼 **CUSTOMER** | `customer1@example.com` | Đặt xe, hủy chuyến, thanh toán chuyến đi | `1,000,000 VND` |
| 🧑‍💼 **CUSTOMER** | `customer2@example.com` | Đặt xe phụ, kiểm thử tài khoản mới | `0 VND` |
| 🚗 **DRIVER** | `driver1@example.com` | Nhận Offer, cập nhật trạng thái chuyến, xem ví | `500,000 VND` |
| 🚗 **DRIVER** | `driver2@example.com` | Tài xế dự phòng, kiểm thử cạnh tranh nhận cuốc | `300,000 VND` |
| 👑 **ADMIN** | `admin@example.com` | Quản trị hệ thống, phê duyệt hồ sơ tài xế | — |

---

## 📁 8. Cấu trúc Thư mục Dự án (Project Structure)

```text
ride-hailing-monorepo/
├── apps/
│   ├── api-gateway/                 # Spring Cloud Gateway routing & security filter (Port 8081)
│   ├── user-driver-service/         # Authentication, User & Driver profile management (Port 8085)
│   ├── pricing-service/             # Dynamic fare calculation & quote management (Port 8084)
│   ├── location-service/            # Real-time driver GPS tracking & Redis GEO (Port 8083)
│   ├── trip-dispatch-service/       # Trip lifecycle state machine & matching engine (Port 8086)
│   └── payment-wallet-service/      # Idempotent payments & double-entry wallet ledger (Port 8082)
├── libs/
│   └── shared-core/
│       ├── common-contracts/        # Shared DTOs, Enums, State contracts & Domain Events
│       └── common-security/         # Stateless JWT filter, security autoconfiguration & principal
├── postman/                         # Ready-to-import Postman Collections & Environments
├── scripts/
│   └── sql/                         # Database initialization scripts for all 4 databases
├── docker-compose.yml               # Local infrastructure (PostgreSQL, Redis)
└── pom.xml                          # Root Maven multi-module parent descriptor
```

---

<p align="center">
  <i>Được phát triển với ❤️ trên nền tảng <b>Spring Boot Microservices & Java 21</b></i>
</p>
