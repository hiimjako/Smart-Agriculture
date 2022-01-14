package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.*;
import it.unimore.iot.smartagricolture.mqtt.model.configuration.ZoneSettings;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

import static it.unimore.iot.smartagricolture.mqtt.utils.utils.getNthParamTopic;

public class DataCollectorEmulator {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorEmulator.class);
    private static final Gson gson = new Gson();
    private static final int zoneIdentifier = 1;

    public static void main(String[] args) {
        try {

            DataCollector dataCollector = new DataCollector();

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    MqttConfigurationParameters.BROKER_URL,
                    dataCollector.getId(),
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setCleanSession(true);
            options.setAutomaticReconnect(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            System.out.println("Connected!");
            System.out.println("Start monitoring");

            // Creating the zone --> should be done into the dashboard when the operator installs
            // the devices, same for the default configurations
            dataCollector.createZone(zoneIdentifier);

            // Default lightController: Esempio di configurazione custom, con le luci attive
            LightController defaultLightConfiguration = new LightController();
            defaultLightConfiguration.getActuator().setActive(true);
            dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultLightConfiguration);

            // Default irrigationController: Esempio di configurazione custom
            IrrigationController defaultIrrigationConfiguration = new IrrigationController();
            defaultIrrigationConfiguration.getActuator().setActive(true);
            defaultIrrigationConfiguration.getActivationPolicy().setTimePolicy("16 * 1 * * *");
            defaultIrrigationConfiguration.getActivationPolicy().setDurationMinute(1);
            defaultIrrigationConfiguration.setIrrigationLevel("medium");
            defaultIrrigationConfiguration.setRotate(false);
            dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);

            subscribePresentationTopic(mqttClient, dataCollector);
            subscribeEnvironmentControllerTopic(mqttClient);

            // simulazione di cambio configurazione dopo 10 secondi
            Thread.sleep(5000);
            logger.info("Sending new configuration to lights!");
            LightController newLightConfiguration = new LightController();
            newLightConfiguration.getActuator().setActive(false);

            dataCollector.changeDefaultSettingsZone(zoneIdentifier, newLightConfiguration);
//            sendNewZoneConfigurationToAllLightController(mqttClient, zoneIdentifier, dataCollector);

            Thread.sleep(5000);
            logger.info("Sending new configuration to irrigation!");
            IrrigationController newIrrigationConfiguration = new IrrigationController();
            newIrrigationConfiguration.getActuator().setActive(true);
            newIrrigationConfiguration.getActivationPolicy().setTimePolicy("5 4 * * * *");
            newIrrigationConfiguration.getActivationPolicy().setDurationMinute(1);
            newIrrigationConfiguration.setIrrigationLevel("low");
            newIrrigationConfiguration.setRotate(true);

            dataCollector.changeDefaultSettingsZone(zoneIdentifier, newIrrigationConfiguration);
            sendNewZoneConfigurationToAllIrrigationController(mqttClient, zoneIdentifier, dataCollector);

//            mqttClient.disconnect();
//            mqttClient.close();
//            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TOPICS CALLBACKS

    /**
     * Function that monitor the topics for smartObject presentation
     * Also when an object presents itself it will receive back the latest configuration for that zone
     *
     * @param mqttClient    The mqtt client
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void subscribePresentationTopic(@NotNull IMqttClient mqttClient, DataCollector dataCollector) {
        try {
            int SubscriptionQoS = 1;
            String topicToSubscribe = String.format("%s/+/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.PRESENTATION_TOPIC);

            if (mqttClient.isConnected()) {
//            FIXME: evitabile il thread?
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> new Thread(() -> {
                    try {
                        byte[] payload = msg.getPayload();
                        String sensorType = getNthParamTopic(topic, MqttConfigurationParameters.SENSOR_TOPIC_INDEX);
                        SmartObjectBase smartObjectBase = gson.fromJson(new String(msg.getPayload()), SmartObjectBase.class);

                        switch (sensorType) {
                            case MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), LightController.class));
                            case MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), IrrigationController.class));
                            case MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), EnvironmentalSensor.class));
                        }

                        sendNewZoneConfiguration(mqttClient, zoneIdentifier, smartObjectBase.getId(), dataCollector);
                        logger.info("subscribePresentationTopic -> Message Received (" + topic + ") Message Received: " + new String(payload));
                    } catch (MqttException e) {
                        e.printStackTrace();
                    }
                }).start());
            } else {
                logger.error("Mqtt client not connected");
            }
        } catch (Exception e) {
            logger.error("Error subscribing to configuration topic! Error : " + e.getLocalizedMessage());
        }
    }

    /**
     * Function that monitor the topics related with environmentController class
     *
     * @param mqttClient The mqtt client
     */
    public static void subscribeEnvironmentControllerTopic(@NotNull IMqttClient mqttClient) throws MqttException {
        int SubscriptionQoS = 1;
        String topicSubscribeEnvTelemetry = String.format("%s/%s/+/%s/+/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC,
                MqttConfigurationParameters.TELEMETRY_TOPIC);
        if (mqttClient.isConnected()) {
            mqttClient.subscribe(topicSubscribeEnvTelemetry, (topic, msg) -> {
                byte[] payload = msg.getPayload();
                EnvironmentalSensor environmentalSensor = parseEnvironmentalSensorJsonMessage(payload);

//                if (environmentalSensor != null) {
//                    if (environmentalSensor.getRainSensor().isRaining() || environmentalSensor.getTemperatureSensor().isUnderTemperature()) {
//                        publishActiveStatusActuators(mqttClient, environmentalSensor.getZoneId(), false);
//                    }
//                }

                logger.info("Sensor: {}", environmentalSensor);
//                logger.info("Message Received (" + topic + ") Message Received: " + new String(payload));
            });
        } else {
            logger.error("Mqtt client not connected");
        }
    }

    //  MESSAGES

    /**
     * Function that sends the new configuration to all clients
     * It sets a retained message, so the new client that connects will have
     * the latest configuration on subscription.
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void sendNewZoneConfigurationToAllLightController(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) throws MqttException {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            for (SmartObjectBase smartObject : zoneSettings.getSmartObjects()) {
                if (smartObject instanceof LightController)
                    sendNewZoneConfiguration(mqttClient, zoneId, smartObject.getId(), dataCollector);
            }
        } else {
            logger.error("Default configuration not found");
        }
    }

    /**
     * Function that sends the new configuration to all clients
     * It sets a retained message, so the new client that connects will have
     * the latest configuration on subscription.
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void sendNewZoneConfigurationToAllIrrigationController(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) throws MqttException {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            for (SmartObjectBase smartObject : zoneSettings.getSmartObjects()) {
                if (smartObject instanceof IrrigationController)
                    sendNewZoneConfiguration(mqttClient, zoneId, smartObject.getId(), dataCollector);
            }
        } else {
            logger.error("Default configuration not found");
        }
    }


    /**
     * Function that sends the new configuration to all clients
     * It sets a retained message, so the new client that connects will have
     * the latest configuration on subscription.
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void sendNewZoneConfigurationToAll(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) throws MqttException {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            for (SmartObjectBase smartObject : zoneSettings.getSmartObjects()) {
                sendNewZoneConfiguration(mqttClient, zoneId, smartObject.getId(), dataCollector);
            }
        } else {
            logger.error("Default configuration not found");
        }
    }

    /**
     * Function that sends the new configuration to one client
     * It sets a retained message, so the new client that connects will have
     * the latest configuration on subscription.
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param deviceId      The id of device
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void sendNewZoneConfiguration(@NotNull IMqttClient mqttClient, int zoneId, String deviceId, DataCollector dataCollector) throws MqttException {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            Optional<SmartObjectBase> device = zoneSettings.getSmartObjectById(deviceId);

            if (device.isPresent()) {
                if (device.get() instanceof LightController)
                    sendLightControllerConfiguration(mqttClient, deviceId, zoneSettings.getLightControllerConfiguration());
                else if (device.get() instanceof IrrigationController)
                    sendIrrigationControllerConfiguration(mqttClient, deviceId, zoneSettings.getIrrigationControllerConfiguration());
            } else {
                logger.error("Default configuration not found for device " + deviceId);
            }
        } else {
            logger.error("Default configuration not found");
        }
    }

    /**
     * Function that updates the lightController configuration of specific lightController
     *
     * @param mqttClient                   The mqtt client
     * @param deviceId                     The device target where publish the configuration
     * @param lightControllerConfiguration The new lightController configuration
     */
    public static void sendLightControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, LightController lightControllerConfiguration) throws MqttException {
        int messageQoS = 2;
        boolean retained = true;

        String topic = String.format("%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                deviceId,
                MqttConfigurationParameters.CONFIGURATION_TOPIC);

        logger.info("Publishing (sendLightControllerConfiguration) to Topic: {}", topic);
        sendPayload(mqttClient, topic, gson.toJson(lightControllerConfiguration), messageQoS, retained);
    }

    /**
     * Function that updates the irrigationController configuration of specific irrigationController
     *
     * @param mqttClient           The mqtt client
     * @param deviceId             The device target where publish the configuration
     * @param irrigationController The new irrigationController configuration
     */
    public static void sendIrrigationControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, IrrigationController irrigationController) throws MqttException {
        int messageQoS = 2;
        boolean retained = true;

        String topic = String.format("%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                deviceId,
                MqttConfigurationParameters.CONFIGURATION_TOPIC);

        logger.info("Publishing (sendIrrigationControllerConfiguration) to Topic: {}", topic);
        sendPayload(mqttClient, topic, gson.toJson(irrigationController), messageQoS, retained);
    }


    // UTILS

    /**
     * Parse MQTT messages into a DeviceDescriptor object or null in case of error
     *
     * @param payload The message payload to parse
     * @return the parsed EnvironmentalSensor object or null in case or error.
     */
    public static @Nullable EnvironmentalSensor parseEnvironmentalSensorJsonMessage(byte[] payload) {
        try {
            return (EnvironmentalSensor) gson.fromJson(new String(payload), EnvironmentalSensor.class);
        } catch (Exception e) {
            return null;
        }
    }

    private static void sendPayload(@NotNull IMqttClient mqttClient, String topic, String payload) throws MqttException {
        sendPayload(mqttClient, topic, payload, 0, false);
    }

    private static void sendPayload(@NotNull IMqttClient mqttClient, String topic, String payload, int messageQoS) throws MqttException {
        sendPayload(mqttClient, topic, payload, messageQoS, false);
    }

    /**
     * Wrapper for sending a message, only for intelliJ warnings
     *
     * @param mqttClient The mqtt client
     * @param topic      topic where send data
     * @param payload    payload
     * @param messageQoS QoS of message
     * @param retained   if it has to be set retained
     */
    private static void sendPayload(@NotNull IMqttClient mqttClient, String topic, String payload, int messageQoS, boolean retained) throws MqttException {
        if (mqttClient.isConnected() && payload != null && topic != null) {
            MqttMessage msg = new MqttMessage(payload.getBytes());
            msg.setQos(messageQoS);
            msg.setRetained(retained);
            mqttClient.publish(topic, msg);
            logger.info("Payload sent -> Topic : {} Payload: {}", topic, payload);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }
}
