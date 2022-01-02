package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.IrrigationController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IrrigationControllerEmulator {
    private static final int MESSAGE_COUNT = 1000;
    private final static Logger logger = LoggerFactory.getLogger(IrrigationControllerEmulator.class);
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            String zoneId = "1";
            IrrigationController irrigationController = new IrrigationController(zoneId);

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    MqttConfigurationParameters.BROKER_URL,
                    irrigationController.getId(),
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            System.out.println("Connected!");

            String topicActuation = String.format("%s/%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    irrigationController.getZoneId(),
                    MqttConfigurationParameters.IRRIGATION_TOPIC,
                    MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

            mqttClient.subscribe(topicActuation, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                Boolean newActive = parseBooleanJsonMessage(payload);
                if (newActive != null) {
                    irrigationController.setActive(newActive);
                }
                logger.info("Message Received (" + topic + ") Message Received: " + new String(payload));
            });


            publishDeviceInfo(mqttClient, irrigationController);

            for (int i = 0; i < MESSAGE_COUNT; i++) {
//                irrigationController.toggleActivate();
//                publishTelemetryData(mqttClient, irrigationController);
                Thread.sleep(3000);
            }

            mqttClient.disconnect();
            mqttClient.close();
            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the sensor infos Payload to the specified MQTT topic
     *
     * @param mqttClient      The mqtt client
     * @param lightDescriptor Instance of lightDescriptor, to get ids and battery status
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, IrrigationController lightDescriptor) throws MqttException {
        String topic = String.format("%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                lightDescriptor.getZoneId(),
                MqttConfigurationParameters.LIGHT_TOPIC,
                lightDescriptor.getId());

        String payloadString = gson.toJson(lightDescriptor);

        logger.info("Publishing (publishDeviceInfo) to Topic: {} Data: {}", topic, payloadString);
        if (mqttClient.isConnected() && payloadString != null && topic != null) {
            MqttMessage msg = new MqttMessage(payloadString.getBytes());
            msg.setQos(0);
            msg.setRetained(true);
            mqttClient.publish(topic, msg);

            logger.info("Device Data Correctly Published! Topic : " + topic + " Payload:" + payloadString);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }

    /**
     * Send the actuator status (true|false) to the specified MQTT topic
     *
     * @param mqttClient      The mqtt client
     * @param lightDescriptor Instance of lightDescriptor, to get ids and battery status
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishTelemetryData(IMqttClient mqttClient, IrrigationController lightDescriptor) throws MqttException {
        String topic = String.format("%s/%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                lightDescriptor.getZoneId(),
                MqttConfigurationParameters.LIGHT_TOPIC,
                lightDescriptor.getId(),
                MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

        String payloadString = gson.toJson(lightDescriptor.isActive());

        logger.info("Publishing (publishDeviceInfo) to Topic: {} Data: {}", topic, payloadString);
        if (mqttClient.isConnected() && payloadString != null && topic != null) {
            MqttMessage msg = new MqttMessage(payloadString.getBytes());
            msg.setQos(0);
            msg.setRetained(true);
            mqttClient.publish(topic, msg);

            logger.info("Device Data Correctly Published! Topic : " + topic + " Payload:" + payloadString);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }

    /**
     * Parse MQTT messages into a boolean object or null in case of error
     *
     * @param payload The message payload to parse
     * @return the parsed boolean object or null in case or error.
     */
    public static Boolean parseBooleanJsonMessage(byte[] payload) {
        try {
            return (Boolean) gson.fromJson(new String(payload), boolean.class);
        } catch (Exception e) {
            return null;
        }
    }
}
