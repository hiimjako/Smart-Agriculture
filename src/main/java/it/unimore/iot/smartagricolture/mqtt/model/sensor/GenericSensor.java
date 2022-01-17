package it.unimore.iot.smartagricolture.mqtt.model.sensor;

import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public abstract class GenericSensor<T> {
    private T value;

    public GenericSensor() {
    }

    public GenericSensor(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public abstract SenMLRecord getSenMLRecord();

    @Override
    public String toString() {
        return "GenericSensor{" +
                "value=" + value +
                '}';
    }
}

