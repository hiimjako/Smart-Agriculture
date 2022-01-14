package it.unimore.iot.smartagricolture.mqtt.exception;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class NullBooleanSenMLValue extends NullSenMLValue {
    public NullBooleanSenMLValue() {
        super(Boolean.class.getSimpleName());
    }
}