package it.unimore.iot.smartagricolture.mqtt.model.sensor;

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
