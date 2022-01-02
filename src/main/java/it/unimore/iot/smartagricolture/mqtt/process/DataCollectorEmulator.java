package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.EnvironmentalSensor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class DataCollectorEmulator {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorEmulator.class);
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {

            String clientId = UUID.randomUUID().toString();

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    MqttConfigurationParameters.BROKER_URL,
                    clientId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            System.out.println("Connected!");

            String topicSubscribeBatteryLog = String.format("%s/%s/+/+/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    MqttConfigurationParameters.BATTERY_PERCENTAGE_TOPIC);

            mqttClient.subscribe(topicSubscribeBatteryLog, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                logger.info("Message Received (" + topic + ") Message Received: " + new String(payload));
//                logger.info("Device {} with emergency battery level", 1);
            });

            String topicSubscribeEnvTelemetry = String.format("%s/%s/+/%s/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    MqttConfigurationParameters.ENV_SENSOR_TOPIC,
                    MqttConfigurationParameters.TELEMETRY_TOPIC);

            mqttClient.subscribe(topicSubscribeEnvTelemetry, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                EnvironmentalSensor environmentalSensor = parseEnvironmentalSensorJsonMessage(payload);

                if (environmentalSensor != null) {
                    if (environmentalSensor.getRainSensor().isRaining() || environmentalSensor.getTemperatureSensor().isUnderTemperature()) {
                        publishActiveStatusActuators(mqttClient, environmentalSensor.getZoneId(), false);
                    }
                }

                logger.info("Sensor: {}", environmentalSensor);
//                logger.info("Message Received (" + topic + ") Message Received: " + new String(payload));
            });
//            mqttClient.disconnect();
//            mqttClient.close();
//            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parse MQTT messages into a DeviceDescriptor object or null in case of error
     *
     * @param payload The message payload to parse
     * @return the parsed EnvironmentalSensor object or null in case or error.
     */
    public static EnvironmentalSensor parseEnvironmentalSensorJsonMessage(byte[] payload) {
        try {
            return (EnvironmentalSensor) gson.fromJson(new String(payload), EnvironmentalSensor.class);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Send active status to all rain sensor of zone to the specified MQTT topic
     *
     * @param mqttClient The mqtt client
     * @param active     The status to set to all rain actuators
     *                   True: to start all of them
     *                   False: to stop all of them
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishActiveStatusActuators(IMqttClient mqttClient, String zoneId, Boolean active) throws MqttException {
        String topic = String.format("%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                zoneId,
                MqttConfigurationParameters.IRRIGATION_TOPIC,
                MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

        Gson gson = new Gson();
        String payloadString = gson.toJson(active);
        logger.info("Publishing (publishActiveStatusActuators) to Topic: {} Data: {}", topic, payloadString);

        if (mqttClient.isConnected() && payloadString != null && topic != null) {
            MqttMessage msg = new MqttMessage(payloadString.getBytes());
            msg.setQos(2);
            mqttClient.publish(topic, msg);

            logger.info("Actuator status sent -> Topic : {} Payload: {}", topic, payloadString);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }
}
