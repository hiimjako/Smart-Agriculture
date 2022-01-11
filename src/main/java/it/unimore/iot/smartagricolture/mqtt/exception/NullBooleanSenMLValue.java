package it.unimore.iot.smartagricolture.mqtt.exception;

public class NullBooleanSenMLValue extends NullSenMLValue {
    public NullBooleanSenMLValue() {
        super(Boolean.class.getSimpleName());
    }
}