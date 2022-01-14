package it.unimore.iot.smartagricolture.mqtt.model;

import java.util.UUID;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class SmartObjectBase {
    private String id = UUID.randomUUID().toString();
    public static final String SENML_ID = "id";

    public SmartObjectBase() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "SmartObjectBase{" +
                "id='" + id + '\'' +
                '}';
    }
}
