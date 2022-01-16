package it.unimore.iot.smartagricolture.mqtt.model;

import java.util.ArrayList;
import java.util.Optional;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 13/01/2022 - 15:07
 */
public class ZoneSettings {
    private ArrayList<SmartObjectBase> smartObjects = new ArrayList<>();
    private LightController lightControllerConfiguration;
    private IrrigationController irrigationControllerConfiguration;

    public ZoneSettings() {
    }

    public ArrayList<SmartObjectBase> getSmartObjects() {
        return smartObjects;
    }

    public void setSmartObjects(ArrayList<SmartObjectBase> smartObjects) {
        this.smartObjects = smartObjects;
    }


    public LightController getLightControllerConfiguration() {
        return lightControllerConfiguration;
    }

    public void setLightControllerConfiguration(LightController lightControllerConfiguration) {
        this.lightControllerConfiguration = lightControllerConfiguration;
    }

    public IrrigationController getIrrigationControllerConfiguration() {
        return irrigationControllerConfiguration;
    }

    public void setIrrigationControllerConfiguration(IrrigationController irrigationControllerConfiguration) {
        this.irrigationControllerConfiguration = irrigationControllerConfiguration;
    }

    public boolean addSmartObject(SmartObjectBase smartObject) {
        Optional<SmartObjectBase> device = getSmartObjectById(smartObject.getId());
        if (device.isEmpty()) {
            this.smartObjects.add(smartObject);
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
        return this.smartObjects.stream().filter(smartObjectBase -> smartObjectBase.getId().equals(deviceId)).findFirst();
    }

    public <T extends SmartObjectBase> Optional<T> getSmartObjectById(String deviceId, Class<T> type) {
        return Optional.of(type.cast(this.smartObjects.stream().filter(smartObjectBase -> smartObjectBase.getId().equals(deviceId)).findFirst().get()));
    }


}
