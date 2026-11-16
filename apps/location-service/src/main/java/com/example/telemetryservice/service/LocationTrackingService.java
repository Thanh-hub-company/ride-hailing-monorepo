package com.example.telemetryservice.service;

import com.example.telemetryservice.dto.LocationUpdateDto;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class LocationTrackingService {

    private static final String GEO_KEY = "driver:locations";
    private final StringRedisTemplate redisTemplate;

    public LocationTrackingService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void updateDriverLocation(LocationUpdateDto location) {
        redisTemplate.opsForGeo().add(
                GEO_KEY,
                new Point(location.longitude(), location.latitude()),
                location.driverId().toString()
        );
    }

    public List<LocationUpdateDto> findNearbyDrivers(double latitude, double longitude, double radiusKm) {
        Point center = new Point(longitude, latitude);
        Distance radius = new Distance(radiusKm, Metrics.KILOMETERS);
        Circle area = new Circle(center, radius);

        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs.newGeoRadiusArgs()
                .includeCoordinates()
                .sortAscending();

        GeoResults<RedisGeoCommands.GeoLocation<String>> results = redisTemplate.opsForGeo().radius(GEO_KEY, area, args);

        if (results == null) {
            return Collections.emptyList();
        }

        return results.getContent().stream().map(result -> {
            String driverIdStr = result.getContent().getName();
            Point point = result.getContent().getPoint();
            return new LocationUpdateDto(
                    UUID.fromString(driverIdStr),
                    point != null ? point.getY() : 0.0,
                    point != null ? point.getX() : 0.0,
                    null,
                    null,
                    null
            );
        }).toList();
    }

    public void removeDriverLocation(UUID driverId) {
        redisTemplate.opsForGeo().remove(GEO_KEY, driverId.toString());
    }
}