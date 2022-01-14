package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.Time;
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
    private final BooleanActuator actuator = new BooleanActuator();
    private String irrigationLevel = "medium";
    private Time activationPolicy = new Time();
    public static final ArrayList<String> ALLOWED_IRRIGATION_LEVELS = new ArrayList<>(Arrays.asList("low", "medium", "high"));
    private boolean rotate = false;
    private Battery battery = new Battery();

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

    public Battery getBattery() {
        return battery;
    }

    @Override
    public String toString() {
        return "IrrigationController{" +
                "actuator=" + actuator +
                ", irrigationLevel='" + irrigationLevel + '\'' +
                ", activationPolicy=" + activationPolicy +
                ", rotate=" + rotate +
                ", battery=" + battery +
                '}';
    }

    @Override
    public SenMLPack toSenML(IrrigationController object) {
        SenMLPack senMLPack = new SenMLPack();

        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setBn(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLRecord.setN(Battery.SENML_NAME);
        senMLRecord.setU(Battery.SENML_UNIT);
        senMLRecord.setV(this.battery.getBatteryPercentage());
        senMLPack.add(senMLRecord);

        return senMLPack;
    }

    @Override
    public void run() {
        Date nextRun = this.getActivationPolicy().getNextDateToActivate();
        String currentPolicy = this.getActivationPolicy().getTimeSchedule();
        while (true) {
            try {
                // in case of new policy
                if (!currentPolicy.equals(this.getActivationPolicy().getTimeSchedule())) {
                    nextRun = this.getActivationPolicy().getNextDateToActivate();
                    currentPolicy = this.getActivationPolicy().getTimeSchedule();
                    System.out.println(this.getId() + " new policy read");
                }

                if (this.getActuator().isActive()) {
                    //deve runnare
                    if (this.getActivationPolicy().getNextDateToActivate().after(nextRun)) {
                        nextRun = this.getActivationPolicy().getNextDateToActivate();
                        this.getActivationPolicy().setLastRunStart();

                        while (!this.getActivationPolicy().hasToStop()) {
                            System.out.println(this.getId() + " irrigating!");
                            Thread.sleep(this.getActivationPolicy().milliSecondsUntilEnd());
                        }
                    }
                } else {
                    System.out.println(this.getId() + " it will skip this run, probably it's raining");
                }

                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
