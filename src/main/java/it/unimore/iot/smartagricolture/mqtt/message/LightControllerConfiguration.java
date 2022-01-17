package it.unimore.iot.smartagricolture.mqtt.message;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.GenericActuator;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 16/01/2022 - 16:18
 */
public class LightControllerConfiguration {
    private final GenericActuator<Boolean> status = new GenericActuator<>();

    public LightControllerConfiguration() {
    }

    public LightControllerConfiguration(boolean active) {
        this.getStatus().setValue(active);
    }

    public GenericActuator<Boolean> getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "lightControllerSetting{" +
                "actuator=" + status +
                '}';
    }
}
