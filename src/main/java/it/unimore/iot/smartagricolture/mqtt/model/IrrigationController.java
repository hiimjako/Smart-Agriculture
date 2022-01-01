package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.exception.InvalidValue;

import java.util.ArrayList;
import java.util.Arrays;

public class IrrigationController extends SmartObjectWithBattery implements IBooleanActuator {
    private boolean isActive = false;
    private String irrigationLevel = "medium";
    public static final ArrayList<String> allowedIrrigationLevels = new ArrayList<>(Arrays.asList("low", "medium", "high"));
    private boolean rotate = false;

    public IrrigationController() {
    }

    public IrrigationController(boolean isActive, String irrigationLevel, boolean rotate) throws InvalidValue {
        this.isActive = isActive;
        this.setIrrigationLevel(irrigationLevel);
        this.rotate = rotate;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void toggleActivate() {
        this.isActive = !this.isActive;
    }

    public String getIrrigationLevel() {
        return irrigationLevel;
    }

    public void setIrrigationLevel(String irrigationLevel) throws InvalidValue {
        if (allowedIrrigationLevels.contains(irrigationLevel))
            this.irrigationLevel = irrigationLevel;
        else
            throw new InvalidValue("irrigationLevel: '%s' not allowed, must be in (%s)".formatted(irrigationLevel,
                    String.join(", ", allowedIrrigationLevels)));
    }

    public boolean isRotate() {
        return rotate;
    }

    public void setRotate(boolean rotate) {
        this.rotate = rotate;
    }

    @Override
    public String toString() {
        return "IrrigationController{" +
                "isActive=" + isActive +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", rotate=" + rotate +
                '}';
    }
}
