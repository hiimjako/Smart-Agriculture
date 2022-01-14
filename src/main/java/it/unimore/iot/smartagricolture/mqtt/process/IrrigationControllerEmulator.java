package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.IrrigationController;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static it.unimore.iot.smartagricolture.mqtt.utils.SenMLParser.toSenMLJson;

public class IrrigationControllerEmulator {
    private static final int BATTERY_DRAIN = 2;
    private static final int BATTERY_DRAIN_TICK_PERIOD = 1000;
    private final static Logger logger = LoggerFactory.getLogger(IrrigationControllerEmulator.class);
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            IrrigationController irrigationController = new IrrigationController();
            // TODO: to remove
            irrigationController.setId("test-irrigation-1234");
            irrigationController.getBattery().setBatteryPercentage(26);

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

            // Simulation of running
            Thread thread = new Thread(irrigationController);
            thread.start();

//            for (int i = 0; i < 1000000; i++) {
//                logger.info("   IRRIGATION STATUS: " + irrigationController);
//                Thread.sleep(1000);
//            }

            while (irrigationController.getBattery().getBatteryPercentage() > 0) {
                irrigationController.getBattery().decreaseBatteryLevelBy(BATTERY_DRAIN);
                publishDeviceTelemetry(mqttClient, irrigationController);
                Thread.sleep(BATTERY_DRAIN_TICK_PERIOD);
            }

            thread.stop();
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
     * Send the sensor sensors values Payload to the specified MQTT topic in SenML format
     *
     * @param mqttClient           The mqtt client
     * @param irrigationDescriptor Instance of IrrigationController, to get ids and battery status
     */
    public static void publishDeviceTelemetry(IMqttClient mqttClient, IrrigationController irrigationDescriptor) {
        try {
            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    irrigationDescriptor.getId(),
                    MqttConfigurationParameters.TELEMETRY_TOPIC);

            SenMLPack senml = irrigationDescriptor.toSenML(irrigationDescriptor);
            Optional<String> payload = toSenMLJson(senml);

            if (mqttClient.isConnected() && payload.isPresent() && topic != null) {
                String payloadString = payload.get();
                MqttMessage msg = new MqttMessage(payloadString.getBytes());
                msg.setQos(0);
                msg.setRetained(false);
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
}
