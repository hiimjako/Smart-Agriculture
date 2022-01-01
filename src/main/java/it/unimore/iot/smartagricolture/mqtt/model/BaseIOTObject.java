package it.unimore.iot.smartagricolture.mqtt.model;

public class BaseIOTObject {
    protected int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "BaseIOTObject{" +
                "id=" + id +
                '}';
    }
}
