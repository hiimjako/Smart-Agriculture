package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.configuration.ZoneSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class DataCollector {
    private final String id = UUID.randomUUID().toString();
    private final Map<Number, ZoneSettings> zonesSettings = new HashMap<>();
    private HashMap<String, Number> deviceZoneMap = new HashMap<>();

    public DataCollector() {
    }

    public String getId() {
        return id;
    }


    public Map<Number, ZoneSettings> getZonesSettings() {
        return zonesSettings;
    }

    // Ipotizzo che la zona non possa non esistere, poiché quando l'utente dalla dashboard inserirà
    // la zona essa dovrà esistere, e si dovranno specificare delle policy di default
    public ZoneSettings getZoneSettings(int zoneId) {
        return zonesSettings.get(zoneId);
    }

    public HashMap<String, Number> getDeviceZoneMap() {
        return deviceZoneMap;
    }

    public void setDeviceZoneMap(HashMap<String, Number> deviceZoneMap) {
        this.deviceZoneMap = deviceZoneMap;
    }

    public ZoneSettings createZone(int zoneId) {
        return this.zonesSettings.computeIfAbsent(zoneId, v -> new ZoneSettings());
    }

    public void addSmartObjectToZone(int zoneId, SmartObjectBase smartObject) {
        boolean hasInserted = this.createZone(zoneId).addSmartObject(smartObject);
        if (hasInserted) {
            this.deviceZoneMap.put(smartObject.getId(), zoneId);
        }
    }

    public void changeDefaultSettingsZone(int zoneId, SmartObjectBase smartObjectBase) {
        ZoneSettings zoneSettings = this.getZoneSettings(zoneId);

        if (zoneSettings != null) {
            if (smartObjectBase instanceof LightController) {
                zoneSettings.setLightControllerConfiguration((LightController) smartObjectBase);
            } else if (smartObjectBase instanceof IrrigationController) {
                zoneSettings.setIrrigationControllerConfiguration((IrrigationController) smartObjectBase);
            }
        } else {
            System.out.println("The zone does not exists!");
        }
    }

    public int getDeviceZone(String deviceId) {
        return (int) this.deviceZoneMap.get(deviceId);
    }
}
