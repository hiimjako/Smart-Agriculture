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
    private Temperature temperatureSensor = new Temperature();
    private Brightness brightnessSensor = new Brightness();
    private Humidity humiditySensor = new Humidity();
    private Rain rainSensor = new Rain();
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

        SenMLRecord senMLRecord = new SenMLRecord();
        //battery
        senMLRecord.setBn(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLRecord.setN(Battery.SENML_NAME);
        senMLRecord.setU(Battery.SENML_UNIT);
        senMLRecord.setV(this.battery.getBatteryPercentage());
        senMLPack.add(senMLRecord);

        senMLPack = new SenMLPack();
        //temperatureSensor
        senMLRecord.setN(Temperature.SENML_NAME);
        senMLRecord.setU(Temperature.SENML_UNIT);
        senMLRecord.setV(this.temperatureSensor.getValue());
        senMLPack.add(senMLRecord);

        senMLPack = new SenMLPack();
        //brightnessSensor
        senMLRecord.setN(Brightness.SENML_NAME);
        senMLRecord.setU(Brightness.SENML_UNIT);
        senMLRecord.setV(this.brightnessSensor.getValue());
        senMLPack.add(senMLRecord);

        senMLPack = new SenMLPack();
        //humiditySensor
        senMLRecord.setN(Humidity.SENML_NAME);
        senMLRecord.setU(Humidity.SENML_UNIT);
        senMLRecord.setV(this.humiditySensor.getValue());
        senMLPack.add(senMLRecord);

        senMLPack = new SenMLPack();
        //rainSensor
        senMLRecord.setN(Rain.SENML_NAME);
        senMLRecord.setVb(this.rainSensor.isRaining());
        senMLPack.add(senMLRecord);

        return senMLPack;
    }
}
