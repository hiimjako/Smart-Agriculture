package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Brightness extends GenericSensor<Double> {
    public static final String SENML_NAME = "brightness";
    public static final String SENML_UNIT = "lm";

    public Brightness() {
    }

    public Brightness(double value) {
        this.setValue(value);
    }

    public SenMLRecord getSenMLRecord() {
        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setN(Brightness.SENML_NAME);
        senMLRecord.setU(Brightness.SENML_UNIT);
        senMLRecord.setV(this.getValue());
        return senMLRecord;
    }

    @Override
    public String toString() {
        return "Brightness{" +
                "value=" + this.getValue() +
                '}';
    }
}
