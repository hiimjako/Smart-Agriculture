package it.unimore.iot.smartagricolture.mqtt.model.sensor;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Brightness {
    private double value;

    public static final String SENML_NAME = "brightness";
    public static final String SENML_UNIT = "lm";

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
