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
    private String manufacturer;
    private String softwareVersion = "1.0.0";
    private String deviceType = "generic";
    private GeoLocation location = new GeoLocation();

    public static final String SENML_ID = "id";

    public SmartObjectBase() {
    }

    protected SmartObjectBase(String id, String manufacturer, String softwareVersion, String deviceType, GeoLocation location) {
        this.id = id;
        this.manufacturer = manufacturer;
        this.softwareVersion = softwareVersion;
        this.deviceType = deviceType;
        this.location = location;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getSoftwareVersion() {
        return softwareVersion;
    }

    public void setSoftwareVersion(String softwareVersion) {
        this.softwareVersion = softwareVersion;
    }

    public String getDeviceType() {
        return deviceType;
    }

    protected void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public GeoLocation getLocation() {
        return location;
    }

    public void setLocation(GeoLocation location) {
        this.location = location;
    }

    public SmartObjectBase getDeviceInfo() {
        return new SmartObjectBase(this.getId(), this.getManufacturer(), this.getSoftwareVersion(), this.getDeviceType(), this.getLocation());
    }

    @Override
    public String toString() {
        return "SmartObjectBase{" +
                "id='" + id + '\'' +
                ", manufacturer='" + manufacturer + '\'' +
                ", softwareVersion='" + softwareVersion + '\'' +
                ", deviceType='" + deviceType + '\'' +
                ", location=" + location +
                '}';
    }
}
