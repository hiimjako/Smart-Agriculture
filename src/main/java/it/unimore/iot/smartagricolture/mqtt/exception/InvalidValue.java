package it.unimore.iot.smartagricolture.mqtt.exception;

public class InvalidValue extends Exception {
    public InvalidValue(String errorMessage){
        super(errorMessage);
    }
}
