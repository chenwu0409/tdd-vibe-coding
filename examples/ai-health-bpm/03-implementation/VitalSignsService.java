package com.aiboss.health.module.vital.service;

import com.aiboss.health.common.exception.BusinessException;
import com.aiboss.health.module.vital.domain.VitalReading;
import com.aiboss.health.module.vital.domain.VitalType;
import com.aiboss.health.module.vital.dto.CreateVitalRequest;
import com.aiboss.health.module.vital.dto.VitalResponse;
import com.aiboss.health.module.vital.repository.VitalReadingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VitalSignsService {

    private final VitalReadingRepository repository;

    // FR-3: validation ranges
    private static final Map<VitalType, Range> VALID_RANGES = Map.of(
        VitalType.BLOOD_PRESSURE, new Range(50, 250, 30, 150),
        VitalType.BLOOD_SUGAR,    new Range(1.0, 30.0, null, null),
        VitalType.WEIGHT,        new Range(20.0, 300.0, null, null),
        VitalType.HEART_RATE,    new Range(30, 220, null, null)
    );

    /**
     * FR-1: User can submit a vital signs reading
     */
    @Transactional
    public VitalResponse create(UUID userId, CreateVitalRequest request) {
        log.info("[VitalSigns] Creating reading: userId={}, type={}", userId, request.type());

        // FR-3: validate ranges
        validate(userId, request);

        // Build entity
        VitalReading reading = VitalReading.builder()
            .id(UUID.randomUUID())
            .userId(userId)
            .type(request.type())
            .value(request.value())
            .recordedAt(request.recordedAt() != null ? request.recordedAt() : OffsetDateTime.now())
            .createdAt(OffsetDateTime.now())
            .build();

        // Persist
        VitalReading saved = repository.save(reading);

        return VitalResponse.from(saved);
    }

    /**
     * FR-3: validate value ranges + value object schema
     */
    private void validate(UUID userId, CreateVitalRequest request) {
        if (request.type() == null) {
            throw new BusinessException("INVALID_TYPE", "type is required");
        }

        // Type-specific value validation
        switch (request.type()) {
            case BLOOD_PRESSURE -> validateBloodPressure(request.value());
            case BLOOD_SUGAR    -> validateScalar(request.value(), "blood_sugar", 1.0, 30.0);
            case WEIGHT         -> validateScalar(request.value(), "weight", 20.0, 300.0);
            case HEART_RATE     -> validateScalar(request.value(), "heart_rate", 30, 220);
        }
    }

    private void validateBloodPressure(Map<String, Object> value) {
        // TC-Vital-013-reverse: empty value rejected
        if (value == null || value.isEmpty() || !value.containsKey("systolic") || !value.containsKey("diastolic")) {
            throw new BusinessException("INVALID_VALUE", "blood_pressure value requires 'systolic' and 'diastolic' fields");
        }

        Object sysObj = value.get("systolic");
        Object diaObj = value.get("diastolic");
        if (!(sysObj instanceof Number) || !(diaObj instanceof Number)) {
            throw new BusinessException("INVALID_VALUE", "systolic and diastolic must be numbers");
        }

        int systolic = ((Number) sysObj).intValue();
        int diastolic = ((Number) diaObj).intValue();

        // TC-Vital-007-exception: systolic out of range (high)
        if (systolic < 50 || systolic > 250) {
            throw new BusinessException("OUT_OF_RANGE",
                "systolic " + systolic + " out of range [50, 250]");
        }

        // TC-Vital-008-exception: diastolic out of range (low)
        if (diastolic < 30 || diastolic > 150) {
            throw new BusinessException("OUT_OF_RANGE",
                "diastolic " + diastolic + " out of range [30, 150]");
        }
    }

    private void validateScalar(Map<String, Object> value, String typeName, double min, double max) {
        if (value == null || value.isEmpty() || !value.containsKey("value")) {
            throw new BusinessException("INVALID_VALUE", typeName + " value requires 'value' field");
        }

        Object val = value.get("value");
        if (!(val instanceof Number)) {
            throw new BusinessException("INVALID_VALUE", typeName + " value must be a number");
        }

        double d = ((Number) val).doubleValue();
        if (d < min || d > max) {
            throw new BusinessException("OUT_OF_RANGE",
                typeName + " value " + d + " out of range [" + min + ", " + max + "]");
        }
    }

    private record Range(double sysMin, double sysMax, Double diaMin, Double diaMax) {}
}
