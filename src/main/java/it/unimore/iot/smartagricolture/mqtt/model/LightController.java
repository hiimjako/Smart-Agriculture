package it.unimore.iot.smartagricolture.mqtt.model;

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



