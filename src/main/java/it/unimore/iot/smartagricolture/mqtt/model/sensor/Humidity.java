package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Humidity extends GenericSensor<Double> {
    public static final String SENML_NAME = "humidity";
    public static final String SENML_UNIT = "%RH";

    public Humidity() {
    }

    public Humidity(double value) {
        this.setValue(value);
    }

    public SenMLRecord getSenMLRecord() {
        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setN(Humidity.SENML_NAME);
        senMLRecord.setU(Humidity.SENML_UNIT);
        senMLRecord.setV(this.getValue());
        return senMLRecord;
    }

    @Override
    public String toString() {
        return "Humidity{" +
                "value=" + this.getValue() +
                '}';
    }
}
