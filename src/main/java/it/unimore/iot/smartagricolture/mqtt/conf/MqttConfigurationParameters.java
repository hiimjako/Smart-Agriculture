package it.unimore.iot.smartagricolture.mqtt.conf;


import io.github.cdimascio.dotenv.Dotenv;

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
    public static final String MQTT_BASIC_TOPIC = "/iot/agriculture";

    //  sensors
    public static final int SENSOR_TOPIC_INDEX = 3;
    public static final String ZONE_TOPIC = "zone";
    public static final String SM_OBJECT_LIGHT_TOPIC = "light";
    public static final String SM_OBJECT_IRRIGATION_TOPIC = "irrigation";
    public static final String SM_OBJECT_ENVIRONMENTAL_TOPIC = "environmental";

    // params
    public static final String PRESENTATION_TOPIC = "info";
    public static final String TELEMETRY_TOPIC = "telemetry";
    public static final String BATTERY_PERCENTAGE_TOPIC = "battery";
    public static final String ACTUATOR_STATUS_TOPIC = "active";

    // constants
    public static final int THRESHOLD_BATTERY_PERCENTAGE = 20;
    public static final int THRESHOLD_RAIN = 10;

}
