package com.udacity.security;

import com.udacity.image.interfaces.ImageService;
import com.udacity.security.data.*;
import com.udacity.security.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    Sensor sensor =  new Sensor("Door", SensorType.DOOR);

    //Sensor sensor2 = new Sensor("Window", SensorType.WINDOW);
    //Sensor sensor3 = new Sensor("Motion", SensorType.MOTION);

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    //Tests Requirement #1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    @DisplayName("Test 1")
    public void alarmArmed_and_sensorActivated_setSystemTo_pendingAlarmStatus() {


        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //Tests Requirement #2. if alarm is armed and a sensor becomes activated and the system is already pending alarm,
    // set the alarm status to alarm.
    @Test
    @DisplayName("Test 2")
    public void alarmArmed_and_sensorActivated_and_systemInPendingAlarmStatus_setAlarmStatusToAlarm(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Tests Requirement #3. If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    @DisplayName("Test 3")
    public void pendingAlarm_and_allSensorsInactive_returnSystemToNoAlarmStatus() {

        sensor.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

    //Tests Requirement #4. If alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest
    @DisplayName("Test 4")
    @ValueSource(booleans = {true, false})
    public void activeAlarm_changeInSensorState_doesNotAffectAlarmState(boolean sensorStatus){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor,sensorStatus);

        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

    }

    //Tests Requirement #5. If a sensor is activated while already active and the system is in pending state,
    //change it to alarm state.
    @Test
    @DisplayName("Test 5")
    public void sensorActivated_whileAlreadyActive_and_SystemPending_changeToAlarmState(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor,true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);

    }

    //Tests Requirement #6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    @DisplayName("Test 6")
    public void sensorDeactivated_whileAlreadyInactive_alarmStateDoesNotChange(AlarmStatus alarmStatus){

        sensor.setActive(false);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        securityService.changeSensorActivationStatus(sensor,false);

        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

    }

    //Tests Requirement #7. If the image service identifies an image containing a cat while the system is armed-home,
    // put the system into alarm status.
    @Test
    @DisplayName("Test 7")
    public void imageServiceIdentifiesImageContainingCat_while_SystemArmedHome_putSystemIntoAlarmStatus(){




    }

}
