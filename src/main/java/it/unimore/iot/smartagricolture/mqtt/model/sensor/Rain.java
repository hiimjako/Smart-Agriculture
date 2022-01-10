package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;

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

    public boolean isRaining() {
        return isRaining(this.value);
    }

    public static boolean isRaining(double rainLevel) {
        return rainLevel < MqttConfigurationParameters.THRESHOLD_RAIN;
    }


    @Override
    public String toString() {
        return "Rain{" +
                "value=" + value +
                '}';
    }
}
