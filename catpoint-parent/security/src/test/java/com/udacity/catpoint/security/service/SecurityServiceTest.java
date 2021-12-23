package com.udacity.catpoint.security.service;


import com.udacity.catpoint.image.service.ImageService;
import com.udacity.catpoint.security.application.StatusListener;
import com.udacity.catpoint.security.data.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.awt.image.BufferedImage;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;
import static org.mockito.internal.verification.VerificationModeFactory.times;

@ExtendWith(MockitoExtension.class)
public class SecurityServiceTest {

    private SecurityService securityService;

    private Sensor sensor;

    private final String random = UUID.randomUUID().toString();

    @Mock
    private StatusListener statusListener;

    @Mock
    private SecurityRepository securityRepository;

    @Mock
    private ImageService imageService;


    @BeforeEach
    void init() {
        securityService = new SecurityService(securityRepository, imageService);
        sensor = new Sensor(random, SensorType.DOOR);
    }

    private Set<Sensor> allSensorsAsASet(int count, boolean status) {
        Set<Sensor> sensors = new HashSet<>();
        for (int i = 0; i < count; i++) {
            sensors.add(new Sensor(random, SensorType.DOOR));
        }

        sensors.forEach(sensor -> sensor.setActive(status));
        return sensors;
    }

      @Test
    void addStatusListener() {
        securityService.addStatusListener(statusListener);
    }

    @Test
    void removeStatusListener() {
        securityService.removeStatusListener(statusListener);
    }

    @Test
    void addSensor() {
        securityService.addSensor(sensor);
    }

    @Test
    void removeSensor() {
        securityService.removeSensor(sensor);
    }
    @Test
//Test 1-----------Armed Alarm with Activated Sensor and Sensor Status_Pending---------
    void ArmedAlarmwithActivatedSensorandSensorStatus_Pending() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.NO_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }
//Test 2-----------Alarm Status Pending with Sensor Inactive_Status_set_to_No Alarm---------
@Test
    
void AlarmStatusPendingwithSensorInactive_Status_set_to_NoAlarm() {
    when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
    sensor.setActive(false);
    securityService.changeSensorActivationStatus(sensor);
    verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
}
//Test 3-----------Armed Alarm with Activated Sensor and Sensor Status_Pending_set_to_OFF---------
    @Test
        void ArmedAlarmwithActivatedSensorandSensorStatus_Pending_set_to_OFF() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }



//Test 4-----------Activated Alarm with Sensors State Changed_Status_set_to_No_Change---------
    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    
    void ActivatedAlarmwithSensorsStateChanged_Status_set_to_No_Change(boolean status) {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

//Test 5-----------Activated Sensor with Pending AlarmStatus_Set_to_Alarm---------
    @Test
    
    void ActivatedSensorwithPendingAlarmStatus_Set_to_Alarm() {
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        sensor.setActive(true);
        securityService.changeSensorActivationStatus(sensor, true);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

//Test 6-----------Deactivated Sensor Status_set_to_No Change---------
    @ParameterizedTest
    @EnumSource(value = AlarmStatus.class, names = {"NO_ALARM", "PENDING_ALARM", "ALARM"})
    
    void DeactivatedSensorStatus_set_to_NoChange(AlarmStatus status) {
        when(securityRepository.getAlarmStatus()).thenReturn(status);
        sensor.setActive(false);
        securityService.changeSensorActivationStatus(sensor, false);

        verify(securityRepository, never()).setAlarmStatus(any(AlarmStatus.class));
    }

//Test 7-----------Cat Detected with Activated Alarm Status_set_to_Alarm---------
    @Test
   
    void  CatDetectedwithActivatedAlarmStatus_set_to_Alarm() {
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.ARMED_HOME);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(true);
        securityService.processImage(catImage);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);
    }

//Test 8--------- Cat Not Detected Alarm Deactivated Status_set_to_No_Alarm-----------
    @Test
    
    void CatNotDetectedAlarmDeactivatedStatus_set_to_No_Alarm() {
        Set<Sensor> sensors = allSensorsAsASet(3, false);
        when(securityRepository.getSensors()).thenReturn(sensors);
        when(imageService.imageContainsCat(any(), ArgumentMatchers.anyFloat())).thenReturn(false);
        securityService.processImage(mock(BufferedImage.class));

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

   //Test 9-----------System Armed Status_set_to_No_Alarm ---------
    @Test
    
    void SystemArmedStatus_set_to_No_Alarm() {
        securityService.setArmingStatus((ArmingStatus.DISARMED));
        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.NO_ALARM);
    }

   //Test 10-----------System Armed then Sensors Deactivated ---------
    @ParameterizedTest
    @EnumSource(value = ArmingStatus.class, names = {"ARMED_HOME", "ARMED_AWAY"})
    
    void SystemArmedthenSensorsDeactivated(ArmingStatus armingStatus) {
        Set<Sensor> sensors = allSensorsAsASet(3, true);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.PENDING_ALARM);
        when(securityRepository.getSensors()).thenReturn(sensors);
        securityService.setArmingStatus(armingStatus);

        securityService.getSensors().forEach(sensor -> assertFalse(sensor.getActive()));
    }

    //Test 11-----------System Deactivated and Sensor Armed Status_set_to_No_Change ---------
    @Test
    
    void SystemDeactivatedandSensorArmedStatus_set_to_No_Change() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.changeSensorActivationStatus(sensor, true);
        verify(securityRepository, never()).setArmingStatus(ArmingStatus.DISARMED);
    }

   //Test 12-----------System Armed Cat Detected Status_set_to_Alarm ---------
    @Test
    
    void SystemArmedCatDetectedStatus_set_to_Alarm() {
        BufferedImage catImage = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        when(imageService.imageContainsCat(any(), anyFloat())).thenReturn(true);
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        securityService.processImage(catImage);
        securityService.setArmingStatus(ArmingStatus.ARMED_HOME);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.ALARM);

    }

   

//Test 13-----------Armed Alarm Sensor Deactivated Status_set_to_Pending ---------
    @Test
    
    void ArmedAlarmSensorDeactivatedStatus_set_to_Pending() {
        when(securityRepository.getArmingStatus()).thenReturn(ArmingStatus.DISARMED);
        when(securityRepository.getAlarmStatus()).thenReturn(AlarmStatus.ALARM);
        securityService.changeSensorActivationStatus(sensor);

        verify(securityRepository, times(1)).setAlarmStatus(AlarmStatus.PENDING_ALARM);
    }

}
