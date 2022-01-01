package it.unimore.iot.smartagricolture.mqtt.model;

public class SmartObjectWithBattery extends BaseIOTObject {
    private int batteryPercentage;
    public static final int BATTERY_PERCENTAGE_THRESHOLD = 20;


    public SmartObjectWithBattery() {
        //load the real object battery
        this.batteryPercentage = 100;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public boolean isUnderThreshold() {
        return this.batteryPercentage < BATTERY_PERCENTAGE_THRESHOLD;
    }

    @Override
    public String toString() {
        return "SmartObjectWithBattery{" +
                "batteryPercentage=" + batteryPercentage +
                '}';
    }
}
