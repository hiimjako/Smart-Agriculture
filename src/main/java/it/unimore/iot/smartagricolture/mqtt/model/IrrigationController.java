package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Time;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class IrrigationController extends SmartObjectBase {
    private final BooleanActuator actuator = new BooleanActuator();
    private String irrigationLevel = "medium";
    private Time activationPolicy = new Time();
    public static final ArrayList<String> ALLOWED_IRRIGATION_LEVELS = new ArrayList<>(Arrays.asList("low", "medium", "high"));

    private boolean rotate = false;

    public IrrigationController() {
    }

    public IrrigationController(Time activationPolicy) {
        this.activationPolicy = activationPolicy;
    }

    public IrrigationController(Time activationPolicy, String irrigationLevel, boolean rotate) {
        this.activationPolicy = activationPolicy;
        this.setIrrigationLevel(irrigationLevel);
        this.rotate = rotate;
    }

    public String getIrrigationLevel() {
        return irrigationLevel;
    }

    public void setIrrigationLevel(String irrigationLevel) {
        if (ALLOWED_IRRIGATION_LEVELS.contains(irrigationLevel))
            this.irrigationLevel = irrigationLevel;
        else
            throw new IllegalArgumentException("irrigationLevel: '%s' not allowed, must be in (%s)".formatted(irrigationLevel,
                    String.join(", ", ALLOWED_IRRIGATION_LEVELS)));
    }

    public Time getActivationPolicy() {
        return activationPolicy;
    }

    public void setActivationPolicy(Time activationPolicy) {
        this.activationPolicy = activationPolicy;
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    public BooleanActuator getActuator() {
        return actuator;
    }

    @Override
    public String toString() {
        return "IrrigationController{" +
                "actuator=" + actuator +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", activationPolicy=" + activationPolicy +
                ", rotate=" + rotate +
                '}';
    }
}
