package it.unimore.iot.smartagricolture.mqtt.message;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Timer;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 16/01/2022 - 16:18
 */
public class IrrigationControllerConfiguration {
    private final BooleanActuator actuator = new BooleanActuator();
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

    public BooleanActuator getActuator() {
        return actuator;
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
                "actuator=" + actuator +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", activationPolicy=" + activationPolicy +
                ", rotate=" + rotate +
                '}';
    }
}


