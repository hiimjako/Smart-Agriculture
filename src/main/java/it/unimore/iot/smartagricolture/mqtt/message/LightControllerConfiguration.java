package it.unimore.iot.smartagricolture.mqtt.message;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 16/01/2022 - 16:18
 */
public class LightControllerConfiguration {
    private final BooleanActuator actuator = new BooleanActuator();

    public LightControllerConfiguration() {
    }

    public LightControllerConfiguration(boolean active) {
        this.getActuator().setActive(active);
    }

    public BooleanActuator getActuator() {
        return actuator;
    }

    @Override
    public String toString() {
        return "lightControllerSetting{" +
                "actuator=" + actuator +
                '}';
    }
}
