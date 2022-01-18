package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.interfaces.ISenMLFormat;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.*;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class EnvironmentalSensor extends SmartObjectBase implements ISenMLFormat {
    private Temperature temperatureSensor = new Temperature(0);
    private Brightness brightnessSensor = new Brightness(0);
    private Humidity humiditySensor = new Humidity(0);
    private Rain rainSensor = new Rain(false);
    private Battery battery = new Battery();

    public EnvironmentalSensor() {
    }


    public EnvironmentalSensor(double temperature, double brightness, double humidity, boolean rainLevel, int batteryPercentage) {
        this.temperatureSensor = new Temperature(temperature);
        this.brightnessSensor = new Brightness(brightness);
        this.humiditySensor = new Humidity(humidity);
        this.rainSensor = new Rain(rainLevel);
        this.battery = new Battery(batteryPercentage);
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


    @Override
    public String toString() {
        return "EnvironmentalSensor{" +
                "temperatureSensor=" + temperatureSensor +
                ", brightnessSensor=" + brightnessSensor +
                ", humiditySensor=" + humiditySensor +
                ", rainSensor=" + rainSensor +
                ", battery=" + battery +
                '}';
    }

    @Override
    public SenMLPack toSenML(Object object) {
        SenMLPack senMLPack = new SenMLPack();

        //battery
        SenMLRecord senMLRecord = this.battery.getSenMLRecord();
        senMLRecord.setBn(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLPack.add(senMLRecord);

        //temperatureSensor
        senMLRecord = this.temperatureSensor.getSenMLRecord();
        senMLPack.add(senMLRecord);

        //brightnessSensor
        senMLRecord = this.brightnessSensor.getSenMLRecord();
        senMLPack.add(senMLRecord);

        //humiditySensor
        senMLRecord = this.humiditySensor.getSenMLRecord();
        senMLPack.add(senMLRecord);

        //rainSensor
        senMLRecord = this.rainSensor.getSenMLRecord();
        senMLPack.add(senMLRecord);

        return senMLPack;
    }
}
