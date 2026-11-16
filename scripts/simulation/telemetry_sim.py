import threading
import time
import requests
import random

def send_telemetry(driver_id):
    url = "http://localhost:8080/api/v1/telemetry"
    while True:
        payload = {
            "driverId": driver_id,
            "lat": 10.7 + random.uniform(-0.01, 0.01),
            "lng": 106.6 + random.uniform(-0.01, 0.01),
            "accuracy": 10.0,
            "timestamp": time.time() * 1000,
            "sequence": int(time.time() * 1000)
        }
        try:
            requests.post(url, json=payload)
        except:
            pass
        time.sleep(3)

# Chạy 100 tài xế (UC-09)
for i in range(100):
    threading.Thread(target=send_telemetry, args=(f"driver_{i}",)).start()
