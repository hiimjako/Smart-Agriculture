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
            IrrigationController irrigationController = new IrrigationController();
            // TODO: to remove
            irrigationController.setId("test-irrigation-1234");

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

            publishDeviceInfo(mqttClient, irrigationController);
            subscribeConfigurationTopic(mqttClient, irrigationController);


            for (int i = 0; i < 1000000; i++) {
                logger.info("   IRRIGATION STATUS: " + irrigationController);
                Thread.sleep(1000);
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
     * @param mqttClient           The mqtt client
     * @param irrigationDescriptor Instance of IrrigationController, to get ids and battery status
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, IrrigationController irrigationDescriptor) {
        try {
            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    irrigationDescriptor.getId(),
                    MqttConfigurationParameters.PRESENTATION_TOPIC);

            String payloadString = gson.toJson(irrigationDescriptor);
            if (mqttClient.isConnected() && payloadString != null && topic != null) {
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
     * Function that subscribes the object to the configuration topic
     * So here where it receives the behaviour updates
     *
     * @param mqttClient           The mqtt client
     * @param irrigationController Instance of IrrigationController
     */
    public static void subscribeConfigurationTopic(IMqttClient mqttClient, IrrigationController irrigationController) {
        try {
            int SubscriptionQoS = 1;
            String topicToSubscribe = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    irrigationController.getId(),
                    MqttConfigurationParameters.CONFIGURATION_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    byte[] payload = msg.getPayload();
                    IrrigationController newConfiguration = gson.fromJson(new String(payload), IrrigationController.class);
                    // TODO: parsing new data, also rotation and level?

                    irrigationController.getActuator().setActive(newConfiguration.getActuator().isActive());
                    irrigationController.setIrrigationLevel(newConfiguration.getIrrigationLevel());
                    irrigationController.setRotate(newConfiguration.isRotate());
                    irrigationController.setActivationPolicy(newConfiguration.getActivationPolicy());
                    logger.info("New configuration received on: (" + topic + ")  with: " + newConfiguration);
                });
            } else {
                logger.error("Mqtt client not connected");
            }
        } catch (Exception e) {
            logger.error("Error subscribing to configuration topic! Error : " + e.getLocalizedMessage());
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
