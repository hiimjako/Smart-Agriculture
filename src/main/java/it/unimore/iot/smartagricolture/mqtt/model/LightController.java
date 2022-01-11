package it.unimore.iot.smartagricolture.mqtt.model;

import it.unimore.iot.smartagricolture.mqtt.exception.NullBooleanSenMLValue;
import it.unimore.iot.smartagricolture.mqtt.exception.NullStringSenMLValue;
import it.unimore.iot.smartagricolture.mqtt.model.actuator.BooleanActuator;
import it.unimore.iot.smartagricolture.mqtt.model.interfaces.ISenMLFormat;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 02/01/2022 - 16:18
 */
public class LightController extends SmartObjectBase implements ISenMLFormat<LightController> {
    private final BooleanActuator actuator = new BooleanActuator();


    public static final String SENML_ID = "id";
    public static final String SENML_VALUE = "value";

    public LightController() {
    }

    public BooleanActuator getActuator() {
        return actuator;
    }

    @Override
    public SenMLPack toSenML(LightController object) {
        SenMLPack senMLPack = new SenMLPack();

        SenMLRecord senMLRecord = new SenMLRecord();
        senMLRecord.setBn(this.getId());
        senMLRecord.setN(SENML_ID);
        senMLRecord.setVs(this.getId());
        senMLRecord.setBt(System.currentTimeMillis());
        senMLPack.add(senMLRecord);

        senMLRecord = new SenMLRecord();
        senMLRecord.setN(SENML_VALUE);
        senMLRecord.setVb(this.getActuator().isActive());
        senMLPack.add(senMLRecord);

        return senMLPack;
    }

    @Override
    public LightController parseSenML(SenMLPack senMLPack) {
        try {
            LightController lightController = new LightController();
            String baseName = null;
            Number baseTime = null;

            for (SenMLRecord record : senMLPack) {
                baseName = record.getBn() != null ? record.getBn() : baseName;
                baseTime = record.getBt() != null ? record.getBt() : baseTime;

                String name = record.getN();
                Boolean booleanValue = record.getVb();
                String stringValue = record.getVs();

                if (baseName != null) {
                    name += baseName;
                }


                if (name != null) {
                    switch (name) {
                        case LightController.SENML_ID -> {
                            if (stringValue == null)
                                throw new NullStringSenMLValue();
                            lightController.setId(stringValue);

                        }
                        case LightController.SENML_VALUE -> {
                            if (booleanValue == null)
                                throw new NullBooleanSenMLValue();
                            lightController.actuator.setActive(booleanValue);
                        }
                    }
                }
            }

        } catch (Exception e) {
            System.out.println(e);
            return null;
        }
        return null;
    }

    @Override
    public String toString() {
        return "LightController{" +
                "actuator=" + actuator +
                '}';
    }
}



