package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class Rain extends GenericSensor<Boolean> {

    public static final String SENML_NAME = "rain";

    public Rain() {
    }

    public Rain(boolean value) {
        this.setValue(value);
    }

    public boolean isRaining() {
        return this.getValue();
    }

    public SenMLRecord getSenMLRecord() {
        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setN(SENML_NAME);
        senMLRecord.setVb(this.isRaining());
        return senMLRecord;
    }

    @Override
    public String toString() {
        return "Rain{" +
                "value=" + this.getValue() +
                '}';
    }
}
