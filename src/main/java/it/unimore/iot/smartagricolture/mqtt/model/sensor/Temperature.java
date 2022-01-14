package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Temperature {
    private double value;

    public static final String SENML_NAME = "temperature";
    public static final String SENML_UNIT = "Cel";


    public Temperature() {
    }

    public Temperature(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public boolean isUnderTemperature() {
        return this.value >= MqttConfigurationParameters.THRESHOLD_TEMPERATURE_CEL;
    }

    public static boolean isUnderTemperature(int value) {
        return value >= MqttConfigurationParameters.THRESHOLD_TEMPERATURE_CEL;
    }


    @Override
    public String toString() {
        return "Temperature{" +
                "value=" + value +
                '}';
    }
}
