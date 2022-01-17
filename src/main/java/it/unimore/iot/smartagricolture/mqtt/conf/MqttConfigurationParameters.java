package it.unimore.iot.smartagricolture.mqtt.conf;


import io.github.cdimascio.dotenv.Dotenv;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class MqttConfigurationParameters {
    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    // mqtt
    public static String BROKER_ADDRESS = dotenv.get("BROKER_ADDRESS", "127.0.0.1");
    public static int BROKER_PORT = Integer.parseInt(dotenv.get("BROKER_PORT", "1883"));
    public static String BROKER_URL = String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT);
    public static String MQTT_USERNAME = dotenv.get("MQTT_USERNAME");
    public static String MQTT_PASSWORD = dotenv.get("MQTT_PASSWORD");

    // Mqtt topic
    public static final String MQTT_BASIC_TOPIC = dotenv.get("MQTT_BASIC_TOPIC", "/iot/agriculture/device");

    //  sensors
    public static final int SENSOR_TOPIC_INDEX = Integer.parseInt(dotenv.get("SENSOR_TOPIC_INDEX", "4"));
    public static final String SM_OBJECT_LIGHT_TOPIC = dotenv.get("SM_OBJECT_LIGHT_TOPIC", "light");
    public static final String SM_OBJECT_IRRIGATION_TOPIC = dotenv.get("SM_OBJECT_IRRIGATION_TOPIC", "irrigation");
    public static final String SM_OBJECT_ENVIRONMENTAL_TOPIC = dotenv.get("SM_OBJECT_ENVIRONMENTAL_TOPIC", "environmental");

    // params
    public static final String PRESENTATION_TOPIC = dotenv.get("PRESENTATION_TOPIC", "info");
    public static final String CONFIGURATION_TOPIC = dotenv.get("CONFIGURATION_TOPIC", "setting");
    public static final String TELEMETRY_TOPIC = dotenv.get("TELEMETRY_TOPIC", "telemetry");

    // constants
    public static final int THRESHOLD_BATTERY_PERCENTAGE = Integer.parseInt(dotenv.get("THRESHOLD_BATTERY_PERCENTAGE", "20"));
    public static final double THRESHOLD_TEMPERATURE_CEL = Double.parseDouble(dotenv.get("THRESHOLD_TEMPERATURE_CEL", "10.0"));

}
