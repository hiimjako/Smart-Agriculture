package it.unimore.iot.smartagricolture.mqtt.exception;

public class NullSenMLValue extends Exception {
    public NullSenMLValue(String type) {
        super("Expected %s, instead received null".formatted(type);
    }
}


