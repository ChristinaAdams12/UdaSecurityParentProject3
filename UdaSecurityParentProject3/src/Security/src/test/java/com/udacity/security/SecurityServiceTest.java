package com.udacity.security;

import com.udacity.image.interfaces.ImageService;
import com.udacity.security.data.*;
import com.udacity.security.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private  SecurityService securityService;

    Sensor sensor1 =  new Sensor("Front door", SensorType.DOOR);
    Sensor sensor2 = new Sensor("Window", SensorType.WINDOW);
    Sensor sensor3 = new Sensor("Motion", SensorType.MOTION);

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);

    }

    //Tests Requirement #1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @Test
    public void alarmArmed_and_sensorActivated_setSystemTo_pendingAlarmStatus() {


        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //Tests Requirement #2. if alarm is armed and a sensor becomes activated and the system is already pending alarm,
    // set the alarm status to alarm.
    @Test
    public void alarmArmed_and_sensorActivated_and_systemInPendingAlarmStatus_setAlarmStatusToAlarm(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_AWAY);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor2, true);

        verify(securityRepository).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Tests Requirement #3. If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    public void pendingAlarm_and_allSensorsInactive_returnSystemToNoAlarmStatus() {

        /*
        sensor1.setActive(true);
        sensor2.setActive(true);
        sensor3.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1, false);
        securityService.changeSensorActivationStatus(sensor2, false);
        securityService.changeSensorActivationStatus(sensor3, false);
         */

        sensor1.setActive(true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1, false);
        
        verify(securityRepository).setAlarmStatus(AlarmStatus.NO_ALARM);

    }


}
