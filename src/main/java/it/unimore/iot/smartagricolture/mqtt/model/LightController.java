package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.GenericActuator;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class LightController extends SmartObjectBase {
    private final GenericActuator<Boolean> status = new GenericActuator<>(false);

    public LightController() {
    }

    public GenericActuator<Boolean> getStatus() {
        return status;
    }

    @Override
    public String toString() {
        return "LightController{" +
                "actuator=" + status +
                '}';
    }
}



