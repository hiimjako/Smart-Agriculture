package it.unimore.iot.smartagricolture.mqtt.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataCollector {
    private String id = UUID.randomUUID().toString();
    private Map<Number, ArrayList<SmartObjectBase>> smartObjects = new HashMap<>();

    public DataCollector() {
    }

    public String getId() {
        return id;
    }

    public Map<Number, ArrayList<SmartObjectBase>> getSmartObjects() {
        return smartObjects;
    }

    private ArrayList<SmartObjectBase> createZone(int zoneId) {
        return this.smartObjects.computeIfAbsent(zoneId, v -> new ArrayList<>());
    }

    public void addSmartObjectToZone(int zoneId, SmartObjectBase smartObject) {
        this.createZone(zoneId).add(smartObject);
    }
}
