package it.unimore.iot.smartagricolture.mqtt.model.sensor;

public class Humidity {
    private double value;

    public Humidity() {
    }

    public Humidity(double value) {
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
        return "Humidity{" +
                "value=" + value +
                '}';
    }
}
