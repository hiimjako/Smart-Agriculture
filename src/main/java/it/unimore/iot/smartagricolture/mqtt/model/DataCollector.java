package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.model.configuration.ZoneSettings;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DataCollector {
    private final String id = UUID.randomUUID().toString();
    private final Map<Number, ZoneSettings> zonesSettings = new HashMap<>();

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

    public ZoneSettings createZone(int zoneId) {
        return this.zonesSettings.computeIfAbsent(zoneId, v -> new ZoneSettings());
    }

    public void addSmartObjectToZone(int zoneId, SmartObjectBase smartObject) {
        this.createZone(zoneId).addSmartObject(smartObject);
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
}
