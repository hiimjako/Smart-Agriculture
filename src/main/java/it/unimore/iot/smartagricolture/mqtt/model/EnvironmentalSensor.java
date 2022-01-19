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
    private final Temperature temperatureSensor = new Temperature(0);
    private final Brightness brightnessSensor = new Brightness(0);
    private final Humidity humiditySensor = new Humidity(0);
    private final Rain rainSensor = new Rain(false);
    private final Battery battery = new Battery();
    public static final String DEVICE_TYPE = "environmental";

    public EnvironmentalSensor() {
        super();
        this.setDeviceType(DEVICE_TYPE);
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
    public SenMLPack toSenML() {
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
