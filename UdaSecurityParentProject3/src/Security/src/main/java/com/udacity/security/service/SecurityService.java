package com.udacity.security.service;

import com.udacity.image.interfaces.ImageService;
import com.udacity.security.application.StatusListener;
import com.udacity.security.data.AlarmStatus;
import com.udacity.security.data.ArmingStatus;
import com.udacity.security.data.SecurityRepository;
import com.udacity.security.data.Sensor;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;

/**
 * Service that receives information about changes to the security system. Responsible for
 * forwarding updates to the repository and making any decisions about changing the system state.
 *
 * This is the class that should contain most of the business logic for our system, and it is the
 * class you will be writing unit tests for.
 */
public class SecurityService {

    private ImageService imageService;
    private SecurityRepository securityRepository;
    private Set<StatusListener> statusListeners = new HashSet<>();

    public SecurityService(SecurityRepository securityRepository, ImageService imageService) {
        this.securityRepository = securityRepository;
        this.imageService = imageService;
    }

    /**
     * Sets the current arming status for the system. Changing the arming status
     * may update both the alarm status.
     * @param armingStatus
     */
    public void setArmingStatus(ArmingStatus armingStatus) {
        //if system is disarmed, alarm status gets set to No Alarm
        if(armingStatus == ArmingStatus.DISARMED) {
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }
        else {
            //sets all sensors inactive
            securityRepository.getSensors().forEach(sensor -> sensor.setActive(false));
            //sets the Arming Status
            securityRepository.setArmingStatus(armingStatus);
        }
    }

    /**
     * Internal method that handles alarm status changes based on whether
     * the camera currently shows a cat.
     * @param cat True if a cat is detected, otherwise false.
     */
    private void catDetected(Boolean cat) {
        //if a cat is detected and Armed Home status is set, set alarm to Alarm status
        if(cat && getArmingStatus() == ArmingStatus.ARMED_HOME) {
            setAlarmStatus(AlarmStatus.ALARM);
        //if no cat is detected and all sensors are inactive, set Alarm status to No Alarm
        } else if(!cat && allSensorsInactive()){
            setAlarmStatus(AlarmStatus.NO_ALARM);
        }

        statusListeners.forEach(sl -> sl.catDetected(cat));
    }

    /**
     * Register the StatusListener for alarm system updates from within the SecurityService.
     * @param statusListener
     */
    public void addStatusListener(StatusListener statusListener) {
        statusListeners.add(statusListener);
    }

    public void removeStatusListener(StatusListener statusListener) {
        statusListeners.remove(statusListener);
    }

    /**
     * Change the alarm status of the system and notify all listeners.
     * @param status
     */
    public void setAlarmStatus(AlarmStatus status) {
        securityRepository.setAlarmStatus(status);
        statusListeners.forEach(sl -> sl.notify(status));
    }

    /**
     * Internal method for updating the alarm status when a sensor has been activated.
     */
    private void handleSensorActivated() {
        if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED) {
            return; //no problem if the system is disarmed
        }
        switch(securityRepository.getAlarmStatus()) {
            case NO_ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.ALARM);
        }
    }

    /**
     * Internal method for updating the alarm status when a sensor has been deactivated
     */
    private void handleSensorDeactivated() {
        switch(securityRepository.getAlarmStatus()) {
            case PENDING_ALARM -> setAlarmStatus(AlarmStatus.NO_ALARM);
            case ALARM -> setAlarmStatus(AlarmStatus.PENDING_ALARM);
        }
    }

    /**
     * Change the activation status for the specified sensor and update alarm status if necessary.
     * @param sensor
     * @param active
     */
    public void changeSensorActivationStatus(Sensor sensor, Boolean active) {
        if (securityRepository.getAlarmStatus() != AlarmStatus.ALARM) {
            //if sensor is not already active and active/true is selected, go to handleSensorActivated
            if(!sensor.getActive() && active) {
                handleSensorActivated();
            //if sensor is already active and active/true is selected, go to handleSensorActivated
            }else if (sensor.getActive() && active) {
                handleSensorActivated();
            //if sensor is already active and not active/false is selected, go to handleSensorDeactivated
            }else if (sensor.getActive() && !active) {
                handleSensorDeactivated();
            //if sensor is not already active and active/false is selected, do nothing
            }else if(!sensor.getActive() && !active) {
                return;
            }
        //if system is disarmed and Alarm status is set to Alarm, go to handleSensorDeactivated
        }else if(securityRepository.getArmingStatus() == ArmingStatus.DISARMED &&
                securityRepository.getAlarmStatus() == AlarmStatus.ALARM){
                handleSensorDeactivated();
        }
            sensor.setActive(active);
            securityRepository.updateSensor(sensor);
    }

    //checks if all sensors are inactive
    public boolean allSensorsInactive() {
        return securityRepository.getSensors().stream().noneMatch((Sensor::getActive));
    }

    /**
     * Send an image to the SecurityService for processing. The securityService will use its provided
     * ImageService to analyze the image for cats and update the alarm status accordingly.
     * @param currentCameraImage
     */
    public void processImage(BufferedImage currentCameraImage) {
        catDetected(imageService.imageContainsCat(currentCameraImage, 50.0f));
    }

    public AlarmStatus getAlarmStatus() {
        return securityRepository.getAlarmStatus();
    }

    public Set<Sensor> getSensors() {
        return securityRepository.getSensors();
    }

    public void addSensor(Sensor sensor) {
        securityRepository.addSensor(sensor);
    }

    public void removeSensor(Sensor sensor) {
        securityRepository.removeSensor(sensor);
    }

    public ArmingStatus getArmingStatus() {
        return securityRepository.getArmingStatus();
    }

    public Set<StatusListener> getStatusListeners(){
        return statusListeners;

    }
}


