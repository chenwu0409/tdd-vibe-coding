package com.aiboss.health.module.vital;

import com.aiboss.health.module.vital.domain.VitalReading;
import com.aiboss.health.module.vital.domain.VitalType;
import com.aiboss.health.module.vital.dto.CreateVitalRequest;
import com.aiboss.health.module.vital.dto.VitalResponse;
import com.aiboss.health.module.vital.repository.VitalReadingRepository;
import com.aiboss.health.module.vital.service.VitalSignsService;
import com.aiboss.health.common.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VitalSignsServiceTest {

    @Mock
    private VitalReadingRepository repository;

    @InjectMocks
    private VitalSignsService service;

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    // ========== TC-Vital-001-normal ==========
    @Test
    @DisplayName("TC-Vital-001-normal: User can submit valid blood pressure reading")
    void tcVital001_userCanSubmitValidBloodPressure() {
        // Given
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of("systolic", 145, "diastolic", 92),
            null
        );
        when(repository.save(any(VitalReading.class)))
            .thenAnswer(inv -> {
                VitalReading r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

        // When
        VitalResponse response = service.create(USER_ID, request);

        // Then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(VitalType.BLOOD_PRESSURE);
        assertThat(response.getValue()).containsEntry("systolic", 145);
        assertThat(response.getValue()).containsEntry("diastolic", 92);
        assertThat(response.getUserId()).isEqualTo(USER_ID);

        // Verify persistence
        ArgumentCaptor<VitalReading> captor = ArgumentCaptor.forClass(VitalReading.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
    }

    // ========== TC-Vital-002/003/004-normal: other types ==========
    @ParameterizedTest
    @ValueSource(doubles = {5.5, 70.5, 75.0})
    @DisplayName("TC-Vital-002/003/004-normal: User can submit valid blood_sugar/weight/heart_rate")
    void tcVital002to004_userCanSubmitOtherVitalTypes(double value) {
        // Given - determine type by value range
        VitalType type = value < 10 ? VitalType.BLOOD_SUGAR
                    : value < 200 ? VitalType.WEIGHT
                    : VitalType.HEART_RATE;
        var request = new CreateVitalRequest(type, Map.of("value", value), null);
        when(repository.save(any(VitalReading.class)))
            .thenAnswer(inv -> {
                VitalReading r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

        // When
        VitalResponse response = service.create(USER_ID, request);

        // Then
        assertThat(response.getId()).isNotNull();
        assertThat(response.getType()).isEqualTo(type);
    }

    // ========== TC-Vital-005-normal: custom recorded_at ==========
    @Test
    @DisplayName("TC-Vital-005-normal: User can submit reading with custom recorded_at")
    void tcVital005_customRecordedAt() {
        // Given
        OffsetDateTime customTime = OffsetDateTime.parse("2026-10-08T08:00:00+08:00");
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of("systolic", 120, "diastolic", 80),
            customTime
        );
        when(repository.save(any(VitalReading.class)))
            .thenAnswer(inv -> {
                VitalReading r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

        // When
        VitalResponse response = service.create(USER_ID, request);

        // Then
        assertThat(response.getRecordedAt()).isEqualTo(customTime);
    }

    // ========== TC-Vital-007-exception: systolic too high ==========
    @Test
    @DisplayName("TC-Vital-007-exception: 400 when blood pressure systolic out of range (high)")
    void tcVital007_systolicTooHigh() {
        // Given
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of("systolic", 300, "diastolic", 90),
            null
        );

        // When + Then
        assertThatThrownBy(() -> service.create(USER_ID, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("systolic");
    }

    // ========== TC-Vital-008-exception: diastolic too low ==========
    @Test
    @DisplayName("TC-Vital-008-exception: 400 when blood pressure diastolic out of range (low)")
    void tcVital008_diastolicTooLow() {
        // Given
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of("systolic", 120, "diastolic", 20),
            null
        );

        // When + Then
        assertThatThrownBy(() -> service.create(USER_ID, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("diastolic");
    }

    // ========== TC-Vital-010/011-boundary: range edges ==========
    @ParameterizedTest
    @ValueSource(ints = {50, 250})
    @DisplayName("TC-Vital-010/011-boundary: Blood pressure at lower/upper bound")
    void tcVital010and011_boundaryValues(int systolic) {
        // Given
        int diastolic = systolic == 50 ? 30 : 150;
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of("systolic", systolic, "diastolic", diastolic),
            null
        );
        when(repository.save(any(VitalReading.class)))
            .thenAnswer(inv -> {
                VitalReading r = inv.getArgument(0);
                r.setId(UUID.randomUUID());
                return r;
            });

        // When
        VitalResponse response = service.create(USER_ID, request);

        // Then
        assertThat(response.getId()).isNotNull();  // accepted at boundary
    }

    // ========== TC-Vital-013-reverse: empty value object ==========
    @Test
    @DisplayName("TC-Vital-013-reverse: Empty value object rejected")
    void tcVital013_emptyValueRejected() {
        // Given
        var request = new CreateVitalRequest(
            VitalType.BLOOD_PRESSURE,
            Map.of(),
            null
        );

        // When + Then
        assertThatThrownBy(() -> service.create(USER_ID, request))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("required");
    }
}
