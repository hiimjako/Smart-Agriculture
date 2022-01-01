package it.unimore.iot.smartagricolture.mqtt.conf;


import io.github.cdimascio.dotenv.Dotenv;

public class MqttConfigurationParameters {
    static Dotenv dotenv = Dotenv.configure()
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    public static String BROKER_ADDRESS = dotenv.get("BROKER_ADDRESS", "\"127.0.0.1\"");
    public static int BROKER_PORT = Integer.parseInt(dotenv.get("BROKER_PORT", "1883"));
    public static String MQTT_USERNAME = dotenv.get("MQTT_USERNAME");
    public static String MQTT_PASSWORD = dotenv.get("MQTT_PASSWORD");
    public static String MQTT_BASIC_TOPIC = "/iot/agriculture/";
    public static String ZONE_TOPIC = "zone";
    public static String ZONE_ID_TOPIC = "zone/%s";
    public static String LIGHT_TOPIC = "light";
    public static String LIGHT_ID_TOPIC = "light/%s";

    public static String ACTUATOR_STATUS_TOPIC = "active";

}
