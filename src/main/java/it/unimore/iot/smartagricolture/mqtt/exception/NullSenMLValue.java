package it.unimore.iot.smartagricolture.mqtt.exception;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class NullSenMLValue extends Exception {
    public NullSenMLValue(String type) {
        super("Expected %s, instead received null".formatted(type));
    }
}


