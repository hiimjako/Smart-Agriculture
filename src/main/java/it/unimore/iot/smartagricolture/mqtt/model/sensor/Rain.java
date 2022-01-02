package it.unimore.iot.smartagricolture.mqtt.model.sensor;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Rain {
    private double value;
    public static final int IS_RAIN_THRESHOLD = 20;

    public Rain() {
    }

    public Rain(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isRaining() {
        return this.value >= IS_RAIN_THRESHOLD;
    }

    @Override
    public String toString() {
        return "Rain{" +
                "value=" + value +
                '}';
    }
}
