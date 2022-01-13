package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.LightController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightControllerEmulator {
    private static final Gson gson = new Gson();
    private final static Logger logger = LoggerFactory.getLogger(LightControllerEmulator.class);


    public static void main(String[] args) {
        try {

            LightController lightController = new LightController();
            lightController.getActuator().setActive(true);
            // TODO: to remove
            lightController.setId("test-1234");

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    MqttConfigurationParameters.BROKER_URL,
                    lightController.getId(),
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            logger.info("Connected!");

            publishDeviceInfo(mqttClient, lightController);
            subscribeConfigurationTopic(mqttClient, lightController);


            for (int i = 0; i < 1000000; i++) {
                logger.info("   LIGHT STATUS: active -> " + lightController.getActuator().isActive());
                Thread.sleep(1000);
            }

            mqttClient.disconnect();
            mqttClient.close();
            logger.info("Disconnected!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Send the sensor infos Payload to the specified MQTT topic
     *
     * @param mqttClient      The mqtt client
     * @param lightDescriptor Instance of LightController
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, LightController lightDescriptor) {
        try {
            if (mqttClient.isConnected()) {
                String topic = String.format("%s/%s/%s/%s",
                        MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                        MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                        lightDescriptor.getId(),
                        MqttConfigurationParameters.PRESENTATION_TOPIC);

                String payloadString = gson.toJson(lightDescriptor);
                MqttMessage msg = new MqttMessage(payloadString.getBytes());
                msg.setQos(0);
                msg.setRetained(true);
                mqttClient.publish(topic, msg);

                logger.info("Device Data Correctly Published! Topic: " + topic + " Payload:" + payloadString);
            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
            }
        } catch (Exception e) {
            logger.error("Error Publishing LightController Information! Error : " + e.getLocalizedMessage());
        }
    }

    /**
     * Send the sensor infos Payload to the specified MQTT topic
     *
     * @param mqttClient      The mqtt client
     * @param lightController Instance of LightController
     */
    public static void subscribeConfigurationTopic(IMqttClient mqttClient, LightController lightController) throws MqttException {
        int SubscriptionQoS = 1;
        String topicToSubscribe = String.format("%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                lightController.getId(),
                MqttConfigurationParameters.CONFIGURATION_TOPIC);

        if (mqttClient.isConnected()) {
            mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                LightController newConfiguration = gson.fromJson(new String(payload), LightController.class);
                lightController.getActuator().setActive(newConfiguration.getActuator().isActive());
                logger.info("New configuration received on: (" + topic + ")  with: " + newConfiguration.getActuator());
            });
        } else {
            logger.error("Mqtt client not connected");
        }
    }
}
