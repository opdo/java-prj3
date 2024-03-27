package com.service.security.service;

import com.service.security.data.*;

import java.util.HashSet;
import java.util.Set;

public class MockSecurityRepository implements SecurityRepository {
    private Set<Sensor> listSensor;
    private AlarmStatus alarmStatus;
    private ArmingStatus armingStatus;

    // Constructor
    public MockSecurityRepository() {
        listSensor = new HashSet<>();
    }

    @Override
    public void addSensor(Sensor sensor) {
        listSensor.add(sensor);
    }

    @Override
    public void removeSensor(Sensor sensor) {
        listSensor.remove(sensor);
    }

    @Override
    public void updateSensor(Sensor sensor) {
        listSensor
                .stream()
                .filter(s -> s.getSensorId() == sensor.getSensorId())
                .forEach(s -> {
                    s.setActive(sensor.getActive());
                    s.setName(sensor.getName());
                    s.setSensorType(sensor.getSensorType());
                });
    }

    @Override
    public void setAlarmStatus(AlarmStatus alarmStatus) {
        this.alarmStatus = alarmStatus;
    }

    @Override
    public void setArmingStatus(ArmingStatus armingStatus) {
        this.armingStatus = armingStatus;
    }

    @Override
    public Set<Sensor> getSensors() {
        return listSensor;
    }

    @Override
    public AlarmStatus getAlarmStatus() {
        return alarmStatus;
    }

    @Override
    public ArmingStatus getArmingStatus() {
        return armingStatus;
    }

    @Override
    public boolean isPreviousCatDetected() {
        return false;
    }

    @Override
    public void setPreviousCatDetected(boolean cat) {

    }
}
