package it.unimore.iot.smartagricolture.mqtt.model.sensor;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Battery {
    private int batteryPercentage;
    public static final int BATTERY_PERCENTAGE_THRESHOLD = 20;
    public static final int BATTERY_PERCENTAGE_MIN = 0;
    public static final int BATTERY_PERCENTAGE_MAX = 100;


    public Battery() {
        //Only for simulation purposes
        this.batteryPercentage = BATTERY_PERCENTAGE_MAX;
//        new Timer().scheduleAtFixedRate(new TimerTask() {
//            @Override
//            public void run() {
//                this.batteryPercentage
//            }
//        }, 0, 5000);
    }

    public Battery(int batteryPercentage) {
        //Only for simulation purposes
        this.setBatteryPercentage(batteryPercentage);
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        //Only for simulation purposes
        if (batteryPercentage <= BATTERY_PERCENTAGE_MIN) {
            this.batteryPercentage = BATTERY_PERCENTAGE_MIN;
        } else if (batteryPercentage >= BATTERY_PERCENTAGE_MAX)
            this.batteryPercentage = BATTERY_PERCENTAGE_MAX;
        else
            this.batteryPercentage = batteryPercentage;
    }

    public void decreaseBatteryLevelBy(int batteryPercentageToDecrease) {
        this.setBatteryPercentage(this.batteryPercentage - batteryPercentageToDecrease);
    }

    public boolean isBatteryUnderThreshold() {
        return this.batteryPercentage < BATTERY_PERCENTAGE_THRESHOLD;
    }

    @Override
    public String toString() {
        return "Battery{" +
                "batteryPercentage=" + batteryPercentage +
                '}';
    }
}
