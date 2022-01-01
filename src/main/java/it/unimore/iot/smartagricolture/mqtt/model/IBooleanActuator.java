package it.unimore.iot.smartagricolture.mqtt.model;

public interface IBooleanActuator {
    void setActive(boolean active);
    boolean isActive();
    void toggleActivate();
}
