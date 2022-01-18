package it.unimore.iot.smartagricolture.mqtt.model.actuator;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class GenericActuator<T> {
    private T value;

    public GenericActuator() {
    }

    public GenericActuator(T value) {
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "GenericActuator{" +
                "value=" + value +
                '}';
    }
}




