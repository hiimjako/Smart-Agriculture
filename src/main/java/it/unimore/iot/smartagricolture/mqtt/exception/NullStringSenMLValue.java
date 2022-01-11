package it.unimore.iot.smartagricolture.mqtt.exception;

public class NullStringSenMLValue extends NullSenMLValue {
    public NullStringSenMLValue() {
        super(String.class.getSimpleName());
    }
}