package com.udacity.security;

import com.udacity.image.interfaces.ImageService;
import com.udacity.security.application.StatusListener;
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

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    private StatusListener statusListener;

    //set of sensors for test purposes
    Set<Sensor> sensors = new HashSet<>();

    Sensor sensor1 = new Sensor("Door", SensorType.DOOR);
    Sensor sensor2 = new Sensor("Window", SensorType.WINDOW);
    Sensor sensor3 = new Sensor("Motion", SensorType.MOTION);

    @BeforeEach
    void init() {

        securityService = new SecurityService(securityRepository, imageService);

        sensors.add(sensor1);
        sensors.add(sensor2);
        sensors.add(sensor3);
    }

    //Tests Requirement #1. If alarm is armed and a sensor becomes activated, put the system into pending alarm status.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 1")
    public void alarmArmed_and_sensorActivated_setSystemTo_pendingAlarmStatus(ArmingStatus armingStatus) {

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

    //Tests Requirement #2. if alarm is armed and a sensor becomes activated and the system is already pending alarm,
    // set the alarm status to alarm.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 2")
    public void alarmArmed_and_sensorActivated_and_systemInPendingAlarmStatus_setAlarmStatusToAlarm(ArmingStatus armingStatus){

        when(securityRepository.getArmingStatus()).thenReturn(armingStatus);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1, true);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Tests Requirement #3. If pending alarm and all sensors are inactive, return to no alarm state.
    @Test
    @DisplayName("Test 3")
    public void pendingAlarm_and_allSensorsInactive_returnSystemToNoAlarmStatus() {

        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1,true);
        securityService.changeSensorActivationStatus(sensor1,false);
        securityService.getAlarmStatus();

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

    //Tests Requirement #4. If alarm is active, change in sensor state should not affect the alarm state.
    @ParameterizedTest
    @DisplayName("Test 4")
    @ValueSource(booleans = {true, false})
    public void activeAlarm_changeInSensorState_doesNotAffectAlarmState(boolean sensorStatus){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor1,sensorStatus);

        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

    }

    //Tests Requirement #5. If a sensor is activated while already active and the system is in pending state,
    //change it to alarm state.
    @Test
    @DisplayName("Test 5")
    public void sensorActivated_whileAlreadyActive_and_SystemPending_changeToAlarmState(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor1,true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor1,true);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    //Tests Requirement #6. If a sensor is deactivated while already inactive, make no changes to the alarm state.
    @ParameterizedTest
    @EnumSource(AlarmStatus.class)
    @DisplayName("Test 6")
    public void sensorDeactivated_whileAlreadyInactive_alarmStateDoesNotChange(AlarmStatus alarmStatus){

        securityService.changeSensorActivationStatus(sensor1,false);
        when(securityRepository.getAlarmStatus()).thenReturn(alarmStatus);
        securityService.changeSensorActivationStatus(sensor1,false);

        verify(securityRepository,never()).setAlarmStatus(any(AlarmStatus.class));

    }

    //Tests Requirement #7. If the image service identifies an image containing a cat while the system is armed-home,
    //put the system into alarm status.
    @Test
    @DisplayName("Test 7")
    public void imageServiceIdentifiesImageContainingCat_while_SystemArmedHome_putSystemIntoAlarmStatus(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

    //Tests Requirement #8. If the image service identifies an image that does not contain a cat, change the status
    //to no alarm as long as the sensors are not active.
    @Test
    @DisplayName("Test 8")
    public void imageServiceIdentifiesImage_doesNotContainCat_changeStatusToNoAlarm_ifSensorsNotActive(){


        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

    //Tests Requirement #9. If the system is disarmed, set the status to no alarm.
    @Test
    @DisplayName("Test 9")
    public void systemDisarmed_setAlarmStatusTo_noAlarm(){

        securityService.setArmingStatus(ArmingStatus.DISARMED);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);

    }

    //Tests Requirement #10. If the system is armed, reset all sensors to inactive.
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_AWAY","ARMED_HOME"})
    @DisplayName("Test 10")
    public void systemArmed_setAllSensorsToInactive(ArmingStatus armingStatus){

        when(securityRepository.getSensors()).thenReturn(sensors);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor1,true);
        securityService.changeSensorActivationStatus(sensor2,true);
        securityService.setArmingStatus(armingStatus);

        assert(securityRepository).getSensors().stream().noneMatch(Sensor::getActive);

    }

    //Tests Requirement #11. If the system is armed-home while the camera shows a cat, set the alarm status to alarm.
    @Test
    @DisplayName("Test 11")
    public void systemArmedHome_and_cameraDisplaysCat_setAlarmStatus_toAlarm(){

        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(),anyFloat())).thenReturn(true);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

    //Tests if system is in Alarm Status and system is Disarmed and a sensor is deactivated, then the
    // system changes to Pending Alarm.
    @Test
    @DisplayName("Test 12")
    public void AlarmActive_and_SystemDisarmed_systemChangesToPendingAlarm(){

        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor1,false);

        verify(securityRepository,times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);

    }

    //Tests if a sensor is added in SecurityService, that it is happening in SecurityRepository
    @Test
    @DisplayName("Test 13")
    public void addSensor_to_securityService_isAddedTo_securityRepository() {

       securityService.addSensor(sensor1);
       verify(securityRepository,times(1)).addSensor(sensor1);

    }

    //Tests if a sensor is removed in SecurityService, that it is happening in SecurityRepository
    @Test
    @DisplayName("Test 14")
    public void removeSensor_from_securityService_isRemovedFrom_securityRepository() {

        securityService.removeSensor(sensor2);
        verify(securityRepository,times(1)).removeSensor(sensor2);

    }
    //Tests if a statusListener is added to SecurityService
    @Test
    @DisplayName("Test 15")
    public void addStatusListener_to_securityService() {

        securityService.addStatusListener(statusListener);

        assert(securityService).getStatusListeners().contains(statusListener);

    }

    //Tests if a statusListener is removed from SecurityService
    @Test
    @DisplayName("Test 16")
    public void removeStatusListener_from_securityService() {

        securityService.addStatusListener(statusListener);
        securityService.removeStatusListener(statusListener);

        assert(securityService).getStatusListeners().isEmpty();

    }

    //Tests if getSenors is called in SecurityService, that it returns sensors from SecurityRepository
    @Test
    @DisplayName("Test 17")
    public void getSensors_calledInSecurityService_returnsSensorsFromSecurityRepository(){

        when(securityService.getSensors()).thenReturn(sensors);

        System.out.println(securityService.getSensors());
        System.out.println(securityRepository.getSensors());

    }



}
