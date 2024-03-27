package com.service.security.service;


import com.service.image.InterfaceImageService;
import com.service.security.data.AlarmStatus;
import com.service.security.data.ArmingStatus;
import com.service.security.data.Sensor;
import com.service.security.data.SensorType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit test for Security-Service.
 */
@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {
    @InjectMocks
    private SecurityService securityService;
    private HashSet<Sensor> listSensor;

    @Mock
    private MockSecurityRepository mockSecurityRepository;

    @Mock
    private InterfaceImageService mockImageService;

    @BeforeEach
    void init() {
        securityService = new SecurityService(mockSecurityRepository, mockImageService);

        // Add a sensor
        listSensor = new HashSet<>();
        listSensor.add(new Sensor("TEST-01", SensorType.DOOR));
        listSensor.add(new Sensor("TEST-02", SensorType.MOTION));
        listSensor.add(new Sensor("TEST-03", SensorType.WINDOW));
        listSensor.add(new Sensor("TEST-04", SensorType.DOOR));
    }

    // 1. If alarm is armed and a sensor becomes activated,
    // put the system into pending alarm status.
    @ParameterizedTest(name = "Test with arming status: {0}")

    // References: https://docs.java.en.sdacademy.pro/software_testing_advanced/parametrized_tests/
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    @DisplayName("1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.")
    public void alarmIsArmed_sensorActivated_systemShouldPendingAlarmStatus(ArmingStatus armingStatus) {
        when(securityService.getArmingStatus()).thenReturn(armingStatus);
        when(securityService.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);

        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        securityService.changeSensorActivationStatus(mySensor, true);

        verify(mockSecurityRepository, times(1)).updateSensor(mySensor);
        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    // 2. If alarm is armed and a sensor becomes activated and the system is already pending alarm,
    // set the alarm status to alarm.
    @ParameterizedTest(name = "Test with arming status: {0}")

    // References: https://docs.java.en.sdacademy.pro/software_testing_advanced/parametrized_tests/
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    @DisplayName("2. If alarm is armed and a sensor becomes activated and the system is already pending alarm, set the alarm status to alarm.")
    public void alarmIsPending_sensorActivated_systemShouldAlarmStatusToAlarm(ArmingStatus armingStatus) {
        when(mockSecurityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(mockSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        securityService.changeSensorActivationStatus(mySensor, true);

        verify(mockSecurityRepository, times(1)).updateSensor(mySensor);
        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 3. If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    @DisplayName("3. If pending alarm and all sensors are inactive, return to no alarm state.")
    public void pendingAlarm_allSensorsInactive_ShouldReturnNoAlarmState() {
        when(mockSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);


        listSensor.forEach(sensor -> {
            sensor.setActive(true);
            securityService.changeSensorActivationStatus(sensor, false);
        });

        int wantedNumberOfInvocations = listSensor.size();
        verify(mockSecurityRepository, times(wantedNumberOfInvocations)).updateSensor(any());
        verify(mockSecurityRepository, times(wantedNumberOfInvocations)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 4. If alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest(name = "Sensor state is: {0}")

    @ValueSource(booleans = {true, false})
    @DisplayName("4. If alarm is active, change in sensor state should not affect the alarm state.")
    public void alarmActive_sensorStateShouldNotAffectAlarmState(boolean sensorState) {
        when(mockSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);

        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        mySensor.setActive(!sensorState);
        securityService.changeSensorActivationStatus(mySensor, sensorState);

        verify(mockSecurityRepository, times(1)).updateSensor(mySensor);
        verify(mockSecurityRepository, never()).setAlarmStatus(any());
    }

    // 5. If a sensor is activated while already active and the system is in pending state,
    // change it to alarm state.
    @Test
    @DisplayName("5. If a sensor is activated while already active and the system is in pending state, change it to alarm state.")
    public void sensorActivated_while_alreadyActive_alarmPendingState_shouldAlarmChangeToAlarmState() {
        // The system is in pending state
        when(mockSecurityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);

        // Sensor already active
        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        mySensor.setActive(true);

        // Active sensor
        securityService.changeSensorActivationStatus(mySensor, true);

        verify(mockSecurityRepository, times(1)).updateSensor(mySensor);
        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @Test
    @DisplayName("6. If a sensor is deactivated while already inactive, make no changes to the alarm state.")
    public void sensorDeactivated_alreadyInactive_shouldNotChangeAlarmState() {
        // Sensor already deactive
        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        mySensor.setActive(false);

        // Deactivated sensor
        securityService.changeSensorActivationStatus(mySensor, false);

        verify(mockSecurityRepository, times(1)).updateSensor(mySensor);
        verify(mockSecurityRepository, never()).setAlarmStatus(any());
    }

    // 7. If the image service identifies an image containing a cat while the system is armed-home,
    // put the system into alarm status.
    @Test
    @DisplayName("7. If the image service identifies an image containing a cat while the system is armed-home, put the system into alarm status.")
    public void imageService_identifyImage_containsCat_systemIsArmedHome_shouldChangeToAlarmStatus() {
        when(mockSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);

        // Image service identifies an image containing a cat
        when(mockImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(mock());

        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 8. If the camera image does not contain a cat,
    // change the status to no alarm as long as the sensors are not active.
    @Test
    @DisplayName("8. If the camera image does not contain a cat, change the status to no alarm as long as the sensors are not active.")
    public void imageService_identifyImage_notContainsCat_sensorAreNotActive_shouldChangeToNoAlarmStatus() {
        // Image service identifies an image NOT containing a cat
        when(mockImageService.imageContainsCat(any(), anyFloat())).thenReturn(false);
        listSensor.forEach(sensor -> sensor.setActive(false));

        securityService.processImage(mock());

        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 9. If the system is disarmed, set the status to no alarm.
    @Test
    @DisplayName("9. If the system is disarmed, set the status to no alarm.")
    public void systemDisarmed_shouldStatusChangeToNoAlarm() {
        securityService.setArmingStatus(ArmingStatus.DISARMED);
        verify(mockSecurityRepository, times(1)).setArmingStatus(ArmingStatus.DISARMED);
        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    // 10. If the system is armed, reset all sensors to inactive.
    @ParameterizedTest(name = "Test with arming status: {0}")

    // References: https://docs.java.en.sdacademy.pro/software_testing_advanced/parametrized_tests/
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY", "ARMED_HOME"})
    @DisplayName("10. If the system is armed, reset all sensors to inactive.")
    public void systemArmed_shouldResetAllSensorsInactive(ArmingStatus armingStatus) {
        when(mockSecurityRepository.getSensors()).thenReturn(listSensor);

        securityService.setArmingStatus(armingStatus);

        verify(mockSecurityRepository, times(1)).setArmingStatus(armingStatus);
        verify(mockSecurityRepository, never()).setAlarmStatus(any());
        verify(mockSecurityRepository, times(listSensor.size())).updateSensor(any());
        securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
    }

    // 11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @Test
    @DisplayName("11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.")
    public void systemArmedHome_cameraShowCat_shouldChangeAlarmStatusToAlarm() {
        when(mockSecurityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(mockImageService.imageContainsCat(any(), anyFloat())).thenReturn(true);

        securityService.processImage(mock());

        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 12. Cat detected and system is in disarmed, then switch to armed home and the alarm should be change to alarm
    @Test
    @DisplayName("12. Cat detected and system is in disarmed, then switch to armed home and the alarm should be change to alarm")
    public void systemIsInDisarmed_andCatDetected_thenSwitchToArmedHome_shouldBeAlarm() {
        // System is in disarmed
        securityService.setArmingStatus(ArmingStatus.DISARMED);

        // Cat detected
        when(mockSecurityRepository.isPreviousCatDetected()).thenReturn(true);

        // Change to armed home
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(mockSecurityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    // 13. Add a sensor
    @Test
    @DisplayName("13. Add a sensor")
    public void addNewSensor_shouldSuccess() {
        // Add sensor
        Sensor newSensor = new Sensor("TEST-ADD", SensorType.WINDOW);
        securityService.addSensor(newSensor);

        // Verify
        verify(mockSecurityRepository, times(1)).addSensor(newSensor);
    }

    // 14. Remove a sensor
    @Test
    @DisplayName("14. Remove a sensor")
    public void removeSensor_shouldSuccess() {
        // Add sensor
        Sensor mySensor = listSensor.stream().findAny().orElseThrow();
        securityService.removeSensor(mySensor);

        // Verify
        verify(mockSecurityRepository, times(1)).removeSensor(mySensor);
    }
}
