package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.message.LightControllerConfiguration;
import it.unimore.iot.smartagricolture.mqtt.model.GeoLocation;
import it.unimore.iot.smartagricolture.mqtt.model.LightController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightControllerEmulator {
    private static final Gson gson = new Gson();
    private final static Logger logger = LoggerFactory.getLogger(LightControllerEmulator.class);
    private static final int TICK_PERIOD = 10000;


    public static void main(String[] args) {
        try {

            LightController lightController = new LightController();
            lightController.getStatus().setValue(true);
            // TODO: to remove
            lightController.setManufacturer("simens");
            lightController.setSoftwareVersion("1.0.1");
            lightController.setLocation(new GeoLocation(24, 15));
            lightController.setId("test-light-1234");

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
                logger.info("Light status: active -> " + lightController.getStatus().getValue());
                Thread.sleep(TICK_PERIOD);
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
     * @param mqttClient The mqtt client
     * @param lightInfo  The info descriptor for presentation
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, LightController lightInfo) {
        try {
            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                    lightInfo.getId(),
                    MqttConfigurationParameters.PRESENTATION_TOPIC);

            String payloadString = gson.toJson(lightInfo.getDeviceInfo());
            if (mqttClient.isConnected() && payloadString != null && topic != null) {
                MqttMessage msg = new MqttMessage(payloadString.getBytes());
                msg.setQos(1);
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
     * Function that subscribes the object to the configuration topic
     * So here where it receives the behaviour updates
     *
     * @param mqttClient      The mqtt client
     * @param lightController Instance of LightController
     */
    public static void subscribeConfigurationTopic(IMqttClient mqttClient, LightController lightController) {
        try {
            int SubscriptionQoS = 2;
            String topicToSubscribe = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                    lightController.getId(),
                    MqttConfigurationParameters.CONFIGURATION_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    byte[] payload = msg.getPayload();
                    String payloadString = new String(payload);
                    LightControllerConfiguration newConfiguration = gson.fromJson(payloadString, LightControllerConfiguration.class);
                    lightController.getStatus().setValue(newConfiguration.getStatus().getValue());
                    logger.info("New configuration received on: (" + topic + ")  with: " + payloadString);
                });
            } else {
                logger.error("Mqtt client not connected");
            }
        } catch (Exception e) {
            logger.error("Error subscribing to configuration topic! Error : " + e.getLocalizedMessage());
        }
    }
}
