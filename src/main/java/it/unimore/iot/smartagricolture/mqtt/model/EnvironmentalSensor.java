package it.unimore.iot.smartagricolture.mqtt.model;

public class EnvironmentalSensor extends BaseIOTObject {
    private double temperature;
    private double brightness;
    private double humidity;
    private double rainLevel;
    private int batteryPercentage;
    private GeoLocation location = new GeoLocation();

    public EnvironmentalSensor() {
    }

    public EnvironmentalSensor(double temperature, double brightness, double humidity, double rainLevel, int batteryPercentage, GeoLocation location) {
        this.temperature = temperature;
        this.brightness = brightness;
        this.humidity = humidity;
        this.rainLevel = rainLevel;
        this.batteryPercentage = batteryPercentage;
        this.location = location;
    }

    public double getTemperature() {
        return temperature;
    }

    public void setTemperature(double temperature) {
        this.temperature = temperature;
    }

    public double getBrightness() {
        return brightness;
    }

    public void setBrightness(double brightness) {
        this.brightness = brightness;
    }

    public double getHumidity() {
        return humidity;
    }

    public void setHumidity(double humidity) {
        this.humidity = humidity;
    }

    public double getRainLevel() {
        return rainLevel;
    }

    public void setRainLevel(double rainLevel) {
        this.rainLevel = rainLevel;
    }

    public int getBatteryPercentage() {
        return batteryPercentage;
    }

    public void setBatteryPercentage(int batteryPercentage) {
        this.batteryPercentage = batteryPercentage;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "EnvironmentalSensor{" +
                "temperature=" + temperature +
                ", brightness=" + brightness +
                ", humidity=" + humidity +
                ", rainLevel=" + rainLevel +
                ", batteryPercentage=" + batteryPercentage +
                ", location=" + location +
                '}';
    }
}
