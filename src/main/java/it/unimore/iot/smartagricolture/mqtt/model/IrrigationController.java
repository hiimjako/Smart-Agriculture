package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.GenericActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Timer;
import it.unimore.iot.smartagricolture.mqtt.model.interfaces.ISenMLFormat;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.Battery;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class IrrigationController extends SmartObjectBase implements Runnable, ISenMLFormat<IrrigationController> {
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
    public SenMLPack toSenML(IrrigationController object) {
        SenMLPack senMLPack = new SenMLPack();

        SenMLRecord senMLRecord = this.battery.getSenMLRecord();
        senMLRecord.setBn(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLPack.add(senMLRecord);

        return senMLPack;
    }

    @Override
    public void run() {
        Date nextRun = this.getActivationPolicy().getNextDateToActivate();
        String currentPolicy = this.getActivationPolicy().getTimeSchedule();
        boolean isIrrigating = false;
        while (this.battery.getValue() > 0) {
            try {
                // in case of new policy
                if (!currentPolicy.equals(this.getActivationPolicy().getTimeSchedule())) {
                    currentPolicy = this.getActivationPolicy().getTimeSchedule();
                    nextRun = this.getActivationPolicy().getNextDateToActivate();
                    System.out.println("    [" + new Date() + "] " + this.getId() + " new policy read, next activation at " + nextRun);
                }

                if (this.getStatus().getValue()) {
                    //deve runnare
                    if (!isIrrigating) {
                        if (nextRun.before(new Date())) {
                            nextRun = this.getActivationPolicy().getNextDateToActivate();
                            this.getActivationPolicy().setLastRunStart();
                            isIrrigating = true;

                            System.out.println("    [" + new Date() + "] " + this.getId() + " irrigating!");
                        }
                    } else {
                        //still irrigating
                        if (this.getActivationPolicy().hasToStop()) {
                            isIrrigating = false;
                            System.out.println("    [" + new Date() + "] " + this.getId() + " current schedule finished, next at " + this.getActivationPolicy().getNextDateToActivate());
                        }
                    }
                } else {
                    if (isIrrigating) {
                        isIrrigating = false;
                        System.out.println("    [" + new Date() + "] " + this.getId() + " stopped before end of schedule, probably it's raining or low temperature");
                    } else {
                        if (nextRun.before(new Date())) {
                            nextRun = this.getActivationPolicy().getNextDateToActivate();
                            this.getActivationPolicy().setLastRunStart();
                            System.out.println("    [" + new Date() + "] " + this.getId() + " it will skip this run (active false), probably it's raining or low temperature");
                        }
                    }
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
