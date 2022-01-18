package it.unimore.iot.smartagricolture.mqtt.message;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.GenericActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Timer;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 16/01/2022 - 16:18
 */
public class IrrigationControllerConfiguration {
    private final GenericActuator<Boolean> status = new GenericActuator<>();
    private String irrigationLevel = "medium";
    private Timer activationPolicy = new Timer();
    private boolean rotate = false;

    public IrrigationControllerConfiguration() {
    }

    public IrrigationControllerConfiguration(String irrigationLevel, Timer activationPolicy, boolean rotate) {
        this.irrigationLevel = irrigationLevel;
        this.activationPolicy = activationPolicy;
        this.rotate = rotate;
    }

    public GenericActuator<Boolean> getStatus() {
        return status;
    }

    public String getIrrigationLevel() {
        return irrigationLevel;
    }

    public void setIrrigationLevel(String irrigationLevel) {
        this.irrigationLevel = irrigationLevel;
    }

    public Timer getActivationPolicy() {
        return activationPolicy;
    }

    public void setActivationPolicy(Timer activationPolicy) {
        this.activationPolicy = activationPolicy;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    @Override
    public String toString() {
        return "irrigationControllerSetting{" +
                "actuator=" + status +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", activationPolicy=" + activationPolicy +
                ", rotate=" + rotate +
                '}';
    }
}


