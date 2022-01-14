package it.unimore.iot.smartagricolture.mqtt.model.sensor;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Rain {
    private boolean value;

    public static final String SENML_NAME = "rain";

    public Rain() {
    }

    public Rain(boolean value) {
        this.value = value;
    }

    public boolean isValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isRaining() {
        return this.value;
    }


    @Override
    public String toString() {
        return "Rain{" +
                "value=" + value +
                '}';
    }
}
