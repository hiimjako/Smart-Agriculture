package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.GenericActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Timer;
import it.unimore.iot.smartagricolture.mqtt.model.interfaces.ISenMLFormat;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.Battery;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class IrrigationController extends SmartObjectBase implements ISenMLFormat {
    private final GenericActuator<Boolean> status = new GenericActuator<>(false);
    private String irrigationLevel = "medium";
    private Timer activationPolicy = new Timer();
    public static final ArrayList<String> ALLOWED_IRRIGATION_LEVELS = new ArrayList<>(Arrays.asList("low", "medium", "high"));
    private boolean rotate = false;
    private final Battery battery = new Battery();

    public IrrigationController() {
    }

    public IrrigationController(Timer activationPolicy) {
        this.activationPolicy = activationPolicy;
    }

    public IrrigationController(Timer activationPolicy, String irrigationLevel, boolean rotate) {
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

    public GenericActuator<Boolean> getStatus() {
        return status;
    }

    public Battery getBattery() {
        return battery;
    }

    @Override
    public String toString() {
        return "IrrigationController{" +
                "status=" + status +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", activationPolicy=" + activationPolicy +
                ", rotate=" + rotate +
                ", battery=" + battery +
                '}';
    }

    @Override
    public SenMLPack toSenML() {
        SenMLPack senMLPack = new SenMLPack();

        SenMLRecord senMLRecord = this.battery.getSenMLRecord();
        senMLRecord.setBn(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLPack.add(senMLRecord);

        return senMLPack;
    }
}
