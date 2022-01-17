package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Temperature extends GenericSensor<Double> {
    public static final String SENML_NAME = "temperature";
    public static final String SENML_UNIT = "Cel";

    public Temperature() {
    }

    public Temperature(double value) {
        this.setValue(value);
    }

    public boolean isUnderTemperature() {
        return this.getValue() >= MqttConfigurationParameters.THRESHOLD_TEMPERATURE_CEL;
    }

    public static boolean isUnderTemperature(Double value) {
        return value >= MqttConfigurationParameters.THRESHOLD_TEMPERATURE_CEL;
    }

    public SenMLRecord getSenMLRecord() {
        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setN(Temperature.SENML_NAME);
        senMLRecord.setU(Temperature.SENML_UNIT);
        senMLRecord.setV(this.getValue());
        return senMLRecord;
    }

    @Override
    public String toString() {
        return "Temperature{" +
                "value=" + this.getValue() +
                '}';
    }
}
