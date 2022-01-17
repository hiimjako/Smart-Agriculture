package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Battery extends GenericSensor<Integer> {
    public static final int BATTERY_PERCENTAGE_MIN = 0;
    public static final int BATTERY_PERCENTAGE_MAX = 100;

    public static final String SENML_UNIT = "%EL";
    public static final String SENML_NAME = "battery";


    public Battery() {
        //Only for simulation purposes
        this.setValue(BATTERY_PERCENTAGE_MAX);
    }

    public Battery(int batteryPercentage) {
        //Only for simulation purposes
        this.setValue(batteryPercentage);
    }

    @Override
    public void setValue(Integer batteryPercentage) {
        if (batteryPercentage <= BATTERY_PERCENTAGE_MIN) {
            super.setValue(BATTERY_PERCENTAGE_MIN);
        } else if (batteryPercentage >= BATTERY_PERCENTAGE_MAX)
            super.setValue(BATTERY_PERCENTAGE_MAX);
        else {
            super.setValue(batteryPercentage);
        }
    }

    public void decreaseBatteryLevelBy(int batteryPercentageToDecrease) {
        this.setValue(this.getValue() - batteryPercentageToDecrease);
    }

    public boolean isBatteryUnderThreshold() {
        return isBatteryUnderThreshold(this.getValue());
    }

    public static boolean isBatteryUnderThreshold(int batteryPercentage) {
        return batteryPercentage < MqttConfigurationParameters.THRESHOLD_BATTERY_PERCENTAGE;
    }

    public SenMLRecord getSenMLRecord() {
        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setN(Battery.SENML_NAME);
        senMLRecord.setU(Battery.SENML_UNIT);
        senMLRecord.setV(this.getValue());
        return senMLRecord;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "batteryPercentage=" + this.getValue() +
                '}';
    }
}
