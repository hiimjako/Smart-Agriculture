package it.unimore.iot.smartagricolture.mqtt.model.actuator;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class BooleanActuator {
    private boolean isActive = false;

    public BooleanActuator() {
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void toggleActivate() {
        this.setActive(!this.isActive());
    }

    @Override
    public String toString() {
        return "BooleanActuator{" +
                "isActive=" + isActive +
                '}';
    }

    ;
}




