package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.message.IrrigationControllerConfiguration;
import it.unimore.iot.smartagricolture.mqtt.message.LightControllerConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 13/01/2022 - 15:07
 */
public class ZoneSettings {
    //    private ArrayList<SmartObjectBase> smartObjects = new ArrayList<>();
    private final HashMap<String, SmartObjectBase> smartObjects = new HashMap<>();
    private LightControllerConfiguration lightControllerConfiguration;
    private IrrigationControllerConfiguration irrigationControllerConfiguration;

    public ZoneSettings() {
    }

    public ArrayList<SmartObjectBase> getSmartObjects() {
        ArrayList<SmartObjectBase> ret = new ArrayList<>();
        for (String key : this.smartObjects.keySet()) {
            ret.add(this.smartObjects.get(key));
        }
        return ret;
    }

//    public void setSmartObjects(ArrayList<SmartObjectBase> smartObjects) {
//        this.smartObjects = smartObjects;
//    }

    public LightControllerConfiguration getLightControllerConfiguration() {
        return lightControllerConfiguration;
    }

    public void setLightControllerConfiguration(LightControllerConfiguration lightControllerConfiguration) {
        this.lightControllerConfiguration = lightControllerConfiguration;
    }

    public IrrigationControllerConfiguration getIrrigationControllerConfiguration() {
        return irrigationControllerConfiguration;
    }

    public void setIrrigationControllerConfiguration(IrrigationControllerConfiguration irrigationControllerConfiguration) {
        this.irrigationControllerConfiguration = irrigationControllerConfiguration;
    }

    public boolean addSmartObject(SmartObjectBase smartObject) {
        Optional<SmartObjectBase> device = getSmartObjectById(smartObject.getId());
        if (device.isEmpty()) {
            this.smartObjects.put(smartObject.getId(), smartObject);
            return true;
        }
        return false;
    }

//    public <T> SmartObjectBase getSmartObjectsByType(T classType) {
//        return this.smartObjects.stream().filter(smartObjectBase -> {
//            return smartObjectBase instanceof classType;
//        })
//    }

    public Optional<SmartObjectBase> getSmartObjectById(String deviceId) {
        SmartObjectBase ret = this.smartObjects.get(deviceId);
        if (ret == null) return Optional.empty();
        return Optional.of(this.smartObjects.get(deviceId));
        // for (SmartObjectBase sm : this.smartObjects) {
        //     if (sm.getId().equals(deviceId)) return Optional.of(sm);
        // }
        // return Optional.empty();
        // stream version can cause: ConcurrentModificationException
        // return this.smartObjects.stream().filter(smartObjectBase -> smartObjectBase.getId().equals(deviceId)).findFirst();
    }

    public <T extends SmartObjectBase> Optional<T> getSmartObjectById(String deviceId, Class<T> type) {
        SmartObjectBase ret = this.smartObjects.get(deviceId);
        if (ret == null) return Optional.empty();
        return Optional.of(type.cast(this.smartObjects.get(deviceId)));

        // for (SmartObjectBase sm : this.smartObjects) {
        //    if (sm.getId().equals(deviceId)) return Optional.of(type.cast(sm));
        //}
        //return Optional.empty();
        // stream version can cause: ConcurrentModificationException
        // return Optional.of(type.cast(this.smartObjects.stream().filter(smartObjectBase -> smartObjectBase.getId().equals(deviceId)).findFirst().get()));
    }
}
