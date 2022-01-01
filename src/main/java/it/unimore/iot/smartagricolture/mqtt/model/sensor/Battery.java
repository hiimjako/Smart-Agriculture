package it.unimore.iot.smartagricolture.mqtt.model.sensor;

public class Battery {
    private int batteryPercentage;
    public static final int BATTERY_PERCENTAGE_THRESHOLD = 20;


    public Battery() {
        //Only for simulation purposes
        this.batteryPercentage = 100;
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
        if (batteryPercentage >= 0 && batteryPercentage <= 100)
            this.batteryPercentage = batteryPercentage;
        else
            System.err.println("Cannot set battery level < 0 or > 0");
    }

    public void decreaseBatteryLevel(int batteryPercentageToDecrease) {
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
