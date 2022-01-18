package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.message.IrrigationControllerConfiguration;
import it.unimore.iot.smartagricolture.mqtt.model.IrrigationController;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import static it.unimore.iot.smartagricolture.mqtt.utils.SenMLParser.toSenMLJson;

public class IrrigationControllerEmulator {
    private static final int BATTERY_DRAIN = 2;
    private static final int BATTERY_DRAIN_TICK_PERIOD = 5000;
    private final static Logger logger = LoggerFactory.getLogger(IrrigationControllerEmulator.class);
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        try {
            IrrigationController irrigationController = new IrrigationController();
            // TODO: to remove
//            irrigationController.setId("test-irrigation-1234");
            irrigationController.getBattery().setValue(96);

            // Simulation of running
            Runnable runnable = simulateRunning(irrigationController);
            Thread thread = new Thread(runnable);
            thread.start();

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


//            for (int i = 0; i < 1000000; i++) {
//                logger.info("   IRRIGATION STATUS: " + irrigationController);
//                Thread.sleep(1000);
//            }

            while (irrigationController.getBattery().getValue() > 0) {
                irrigationController.getBattery().decreaseBatteryLevelBy(BATTERY_DRAIN);
                publishDeviceTelemetry(mqttClient, irrigationController);
                Thread.sleep(BATTERY_DRAIN_TICK_PERIOD);
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
                msg.setQos(1);
                msg.setRetained(true);
                mqttClient.publish(topic, msg);

                logger.info("Device Data Correctly Published! Topic: " + topic + " Payload:" + payloadString);
            } else {
                logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
            }
        } catch (Exception e) {
            logger.error("Error Publishing IrrigationController Information! Error : " + e.getLocalizedMessage());
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
            int SubscriptionQoS = 2;
            String topicToSubscribe = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    irrigationController.getId(),
                    MqttConfigurationParameters.CONFIGURATION_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    byte[] payload = msg.getPayload();
                    IrrigationControllerConfiguration newConfiguration = gson.fromJson(new String(payload), IrrigationControllerConfiguration.class);

                    irrigationController.getStatus().setValue(newConfiguration.getStatus().getValue());
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
     * Function that simulate the irrigation behaviour, in a runnable way to run it into a thread
     *
     * @param irrigationController Instance of IrrigationController
     * @return Runnable
     */
    @Contract(value = "_ -> new", pure = true)
    public static @NotNull Runnable simulateRunning(IrrigationController irrigationController) {
        return () -> {
            logger.info("Started simulation...");
            Date nextRun = irrigationController.getActivationPolicy().getNextDateToActivate();
            String currentPolicy = irrigationController.getActivationPolicy().getTimeSchedule();
            boolean isIrrigating = false;
            while (irrigationController.getBattery().getValue() > 0) {
                try {
                    // in case of new policy
                    if (!currentPolicy.equals(irrigationController.getActivationPolicy().getTimeSchedule())) {
                        currentPolicy = irrigationController.getActivationPolicy().getTimeSchedule();
                        nextRun = irrigationController.getActivationPolicy().getNextDateToActivate();
                        logger.info("[" + new Date() + "] " + irrigationController.getId() + " new policy read, next activation at " + nextRun);
                    }

                    if (irrigationController.getStatus().getValue()) {
                        //deve runnare
                        if (!isIrrigating) {
                            if (nextRun.before(new Date())) {
                                nextRun = irrigationController.getActivationPolicy().getNextDateToActivate();
                                irrigationController.getActivationPolicy().setLastRunStart();
                                isIrrigating = true;

                                logger.info("[" + new Date() + "] " + irrigationController.getId() + " irrigating!");
                            }
                        } else {
                            //still irrigating
                            if (irrigationController.getActivationPolicy().hasToStop()) {
                                isIrrigating = false;
                                logger.info("[" + new Date() + "] " + irrigationController.getId() + " current schedule finished, next at " + irrigationController.getActivationPolicy().getNextDateToActivate());
                            }
                        }
                    } else {
                        if (isIrrigating) {
                            isIrrigating = false;
                            logger.info("[" + new Date() + "] " + irrigationController.getId() + " stopped before end of schedule, probably it's raining or low temperature");
                        } else {
                            if (nextRun.before(new Date())) {
                                nextRun = irrigationController.getActivationPolicy().getNextDateToActivate();
                                irrigationController.getActivationPolicy().setLastRunStart();
                                logger.info("[" + new Date() + "] " + irrigationController.getId() + " it will skip this run (active false), probably it's raining or low temperature");
                            }
                        }
                    }

                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
    }
}
