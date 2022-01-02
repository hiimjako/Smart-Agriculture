package it.unimore.iot.smartagricolture.mqtt.conf;


import io.github.cdimascio.dotenv.Dotenv;

public class MqttConfigurationParameters {
    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    public static String BROKER_ADDRESS = dotenv.get("BROKER_ADDRESS", "127.0.0.1");
    public static int BROKER_PORT = Integer.parseInt(dotenv.get("BROKER_PORT", "1883"));
    public static String BROKER_URL = String.format("tcp://%s:%d", BROKER_ADDRESS, BROKER_PORT);
    public static String MQTT_USERNAME = dotenv.get("MQTT_USERNAME");
    public static String MQTT_PASSWORD = dotenv.get("MQTT_PASSWORD");
    public static String MQTT_BASIC_TOPIC = "/iot/agriculture";
    public static String ZONE_TOPIC = "zone";
    public static String LIGHT_TOPIC = "light";
    public static String IRRIGATION_TOPIC = "irrigation";
    public static String ENV_SENSOR_TOPIC = "environmental";

    public static String TELEMETRY_TOPIC = "telemetry";
    public static String BATTERY_PERCENTAGE_TOPIC = "battery";
    public static String ACTUATOR_STATUS_TOPIC = "active";

}
