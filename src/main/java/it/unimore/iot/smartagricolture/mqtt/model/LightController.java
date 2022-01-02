package it.unimore.iot.smartagricolture.mqtt.model;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class LightController extends BaseIOTObject implements IBooleanActuator {
    private boolean isActive = false;

    public LightController() {
    }

    public LightController(boolean isActive) {
        this.isActive = isActive;
    }

    @Override
    public void setActive(boolean active) {
        this.isActive = active;
    }

    @Override
    public boolean isActive() {
        return this.isActive;
    }

    @Override
    public void toggleActivate() {
        this.isActive = !this.isActive;
    }

    @Override
    public String toString() {
        return "LightController{" +
                "isActive=" + isActive +
                '}';
    }
}



