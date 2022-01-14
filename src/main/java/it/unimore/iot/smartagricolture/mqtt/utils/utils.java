package it.unimore.iot.smartagricolture.mqtt.utils;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class utils {
    /**
     * Function that returns the Nth param from the MQTT topic hierarchy
     *
     * @param topic    The topic to analyze
     * @param position The index of param to get
     * @example getNthParamTopic(" / iot / device / light / 1 ", 3) --> "light"
     */
    public static String getNthParamTopic(String topic, int position) {
        String value = null;
        if (position > 0) {
            try {
                value = topic.split("/")[position];
            } catch (Exception ignored) {

            }
        }
        return value;
    }
}
