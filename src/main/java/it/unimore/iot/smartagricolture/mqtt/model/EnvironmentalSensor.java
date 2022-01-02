package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.sensor.*;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class EnvironmentalSensor extends BaseIOTObject {
    private Temperature temperatureSensor = new Temperature();
    private Brightness brightnessSensor = new Brightness();
    private Humidity humiditySensor = new Humidity();
    private Rain rainSensor = new Rain(Rain.IS_RAIN_THRESHOLD + 10);
    private Battery battery = new Battery();
    private GeoLocation location = new GeoLocation();

    public EnvironmentalSensor() {
    }

    public EnvironmentalSensor(String zoneId) {
        super(zoneId);
    }

    public EnvironmentalSensor(double temperature, double brightness, double humidity, double rainLevel, int batteryPercentage, GeoLocation location) {
        this.temperatureSensor = new Temperature(temperature);
        this.brightnessSensor = new Brightness(brightness);
        this.humiditySensor = new Humidity(humidity);
        this.rainSensor = new Rain(rainLevel);
        this.battery = new Battery(batteryPercentage);
        this.location = location;
    }

    public Temperature getTemperatureSensor() {
        return this.temperatureSensor;
    }

    public Brightness getBrightnessSensor() {
        return this.brightnessSensor;
    }

    public Humidity getHumiditySensor() {
        return this.humiditySensor;
    }

    public Rain getRainSensor() {
        return this.rainSensor;
    }

    public Battery getBattery() {
        return this.battery;
    }

    public GeoLocation getLocation() {
        return this.location;
    }


    @Override
    public String toString() {
        return "EnvironmentalSensor{" +
                "temperatureSensor=" + temperatureSensor +
                ", brightnessSensor=" + brightnessSensor +
                ", humiditySensor=" + humiditySensor +
                ", rainSensor=" + rainSensor +
                ", battery=" + battery +
                ", location=" + location +
                '}';
    }
}
