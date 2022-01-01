package it.unimore.iot.smartagricolture.mqtt.model;

import java.util.UUID;

public class BaseIOTObject {
    private String id = UUID.randomUUID().toString();
    private String zoneId;

    public BaseIOTObject() {
    }

    public BaseIOTObject(String zoneId) {
        this.zoneId = zoneId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getZoneId() {
        return zoneId;
    }

    public void setZoneId(String zoneId) {
        this.zoneId = zoneId;
    }

    @Override
    public String toString() {
        return "BaseIOTObject{" +
                "id='" + id + '\'' +
                ", zoneId='" + zoneId + '\'' +
                '}';
    }
}
