package it.unimore.iot.smartagricolture.mqtt.model.interfaces;

import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 10/01/2022 - 18:51
 */
public interface ISenMLFormat<T> {
    SenMLPack toSenML(T object);

//    T parseSenML(SenMLPack senMLPack);
}
