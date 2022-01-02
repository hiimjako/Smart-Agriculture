package it.unimore.iot.smartagricolture.mqtt.model.sensor;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Rain {
    private double value;

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

    @Override
    public String toString() {
        return "Rain{" +
                "value=" + value +
                '}';
    }
}
