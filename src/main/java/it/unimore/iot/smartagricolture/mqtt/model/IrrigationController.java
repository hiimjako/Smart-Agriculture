package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.exception.InvalidValue;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;

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
    public static final ArrayList<String> allowedIrrigationLevels = new ArrayList<>(Arrays.asList("low", "medium", "high"));
    private boolean rotate = false;

    public IrrigationController() {
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

    public BooleanActuator getActuator() {
        return actuator;
    }

    @Override
    public String toString() {
        return "IrrigationController{" +
                "actuator=" + actuator +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", rotate=" + rotate +
                '}';
    }
}
