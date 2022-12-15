package com.udacity.security;

import com.udacity.image.interfaces.ImageService;
import com.udacity.security.data.*;
import com.udacity.security.service.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    @Mock
    private ImageService imageService;

    @Mock
    private SecurityRepository securityRepository;

    private SecurityService securityService;

    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
    }

    @Test
    public void alarmArmed_and_sensorActivated_setSystemTo_pendingAlarmStatus() {

        Sensor sensor = new Sensor("Front door", SensorType.DOOR);

        securityService.setArmingStatus(ArmingStatus.ARMED_AWAY);
        securityService.changeSensorActivationStatus(sensor, true);
        securityService.setAlarmStatus(AlarmStatus.PENDING_ALARM);

        verify(securityService).setAlarmStatus(AlarmStatus.PENDING_ALARM);


    }

}
