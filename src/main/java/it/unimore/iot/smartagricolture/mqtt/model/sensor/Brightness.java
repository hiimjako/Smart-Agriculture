package it.unimore.iot.smartagricolture.mqtt.model.sensor;

public class Brightness {
    private double value;

    public Brightness() {
    }

    public Brightness(double value) {
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
        return "Brightness{" +
                "value=" + value +
                '}';
    }
}
