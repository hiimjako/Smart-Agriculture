package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.*;
import it.unimore.iot.smartagricolture.mqtt.model.configuration.ZoneSettings;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.Battery;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.Rain;
import it.unimore.iot.smartagricolture.mqtt.model.sensor.Temperature;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLRecord;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Optional;

import static it.unimore.iot.smartagricolture.mqtt.utils.SenMLParser.parseSenMLJson;
import static it.unimore.iot.smartagricolture.mqtt.utils.utils.getNthParamTopic;

public class DataCollectorEmulator {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorEmulator.class);
    private static final boolean sendNewConfigurationDemo = false;
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
            defaultIrrigationConfiguration.getActuator().setActive(false);
            defaultIrrigationConfiguration.getActivationPolicy().setTimeSchedule("01 * * * * *");
            // Test per interruzione prima della fine
//            defaultIrrigationConfiguration.getActivationPolicy().setDurationHour(1);
            defaultIrrigationConfiguration.getActivationPolicy().setDurationSecond(20);
            defaultIrrigationConfiguration.setIrrigationLevel("medium");
            defaultIrrigationConfiguration.setRotate(false);
            dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);

            // Subscribing
            subscribePresentationTopic(mqttClient, dataCollector);
            subscribeIrrigationControllerTelemetryTopic(mqttClient, dataCollector);
            subscribeEnvironmentControllerTelemetryTopic(mqttClient, dataCollector);

            // Send as retained the default configurations
            sendNewZoneConfigurationToAllSmartObjects(mqttClient, zoneIdentifier, dataCollector);

            if (sendNewConfigurationDemo) {
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
                newIrrigationConfiguration.getActivationPolicy().setTimeSchedule("5 4 * * * *");
                newIrrigationConfiguration.getActivationPolicy().setDurationMinute(1);
                newIrrigationConfiguration.setIrrigationLevel("low");
                newIrrigationConfiguration.setRotate(true);

                dataCollector.changeDefaultSettingsZone(zoneIdentifier, newIrrigationConfiguration);
                sendNewZoneConfigurationToAllIrrigationController(mqttClient, zoneIdentifier, dataCollector);
            }


//            mqttClient.disconnect();
//            mqttClient.close();
//            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // TOPICS SUBSCRIPTIONS CALLBACKS

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
                    byte[] payload = msg.getPayload();
                    String sensorType = getNthParamTopic(topic, MqttConfigurationParameters.SENSOR_TOPIC_INDEX);
                    SmartObjectBase smartObjectBase = gson.fromJson(new String(msg.getPayload()), SmartObjectBase.class);

                    switch (sensorType) {
                        case MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), LightController.class));
                        case MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), IrrigationController.class));
                        case MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC -> dataCollector.addSmartObjectToZone(zoneIdentifier, gson.fromJson(new String(msg.getPayload()), EnvironmentalSensor.class));
                    }

                    if (!msg.isRetained()) {
                        sendNewZoneConfiguration(mqttClient, zoneIdentifier, smartObjectBase.getId(), dataCollector);
                    }

                    logger.info("subscribePresentationTopic -> Message Received (" + topic + ") Message Received: " + new String(payload));
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
     * @param mqttClient    The mqtt client
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void subscribeEnvironmentControllerTelemetryTopic(@NotNull IMqttClient mqttClient, DataCollector dataCollector) {
        try {
            int SubscriptionQoS = 0;
            String topicToSubscribe = String.format("%s/%s/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC,
                    MqttConfigurationParameters.TELEMETRY_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    try {
                        byte[] payload = msg.getPayload();
                        String payloadString = new String(payload);
//                        logger.info("Message received (" + topic + ") Message Received: " + payloadString);

                        Optional<SenMLPack> parsedPayload = parseSenMLJson(payloadString);

                        if (parsedPayload.isPresent()) {
                            SenMLPack senMLPack = parsedPayload.get();

                            boolean isRaining = false;
                            boolean isTemperatureUnderThreshold = false;
                            String temperatureUnit = null;
                            String deviceId = null;
                            Number batteryLevel = null;
                            String batteryUnit = null;
                            Number timestamp = new Date().getTime();
                            for (SenMLRecord record : senMLPack) {
                                String baseName = record.getBn();
                                String name = record.getN();
                                Number value = record.getV();
                                String unit = record.getU();
                                Number time = record.getBt();
                                Boolean booleanValue = record.getVb();

                                if (baseName != null) deviceId = baseName;
                                if (name != null) {
                                    if (value != null) {
                                        if (name.equals(Battery.SENML_NAME)) {
                                            batteryLevel = value;
                                            if (unit != null) batteryUnit = unit;
                                        }

                                        if (name.equals(Temperature.SENML_NAME)) {
                                            isTemperatureUnderThreshold = Temperature.isUnderTemperature(value.intValue());
                                            if (unit != null) temperatureUnit = unit;
                                        }
                                    }

                                    if (booleanValue != null) {
                                        if (name.equals(Rain.SENML_NAME)) {
                                            isRaining = booleanValue;
                                        }
                                    }
                                }
                                if (time != null) timestamp = time;
                            }

                            // POST parsing
                            if (deviceId != null && batteryLevel != null) {
                                Optional<EnvironmentalSensor> device = dataCollector.getZoneSettings(zoneIdentifier).getSmartObjectById(deviceId, EnvironmentalSensor.class);
                                if (device.isPresent()) {
                                    device.get().getBattery().setBatteryPercentage(batteryLevel.intValue());
                                    logDeviceBattery(deviceId, batteryLevel.intValue(), batteryUnit, timestamp.longValue());
                                } else {
                                    logger.error("Error reading subscribeEnvironmentControllerTelemetryTopic! missing device: " + deviceId);
                                }
                            }

                            boolean shouldStopIrrigation = isRaining || isTemperatureUnderThreshold;
                            boolean currentStatus = dataCollector.getZoneSettings(zoneIdentifier).getIrrigationControllerConfiguration().getActuator().isActive();
                            // invio la nuova configurazione solo se cambia da quella precedente

                            if (shouldStopIrrigation) {
                                if (currentStatus) {
                                    logger.info("Detected environmental change -> isRaining: {}, isTemperatureUnderThreshold: {}, currentIrrigationStatus: {}",
                                            isRaining,
                                            isTemperatureUnderThreshold,
                                            currentStatus
                                    );
                                    setActivationIrrigationByZone(mqttClient, zoneIdentifier, false, dataCollector);
                                }
                            } else {
                                if (!currentStatus) {
                                    logger.info("Detected environmental change -> isRaining: {}, isTemperatureUnderThreshold: {}, currentIrrigationStatus: {}",
                                            isRaining,
                                            isTemperatureUnderThreshold,
                                            currentStatus
                                    );
                                    setActivationIrrigationByZone(mqttClient, zoneIdentifier, true, dataCollector);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error reading subscribeEnvironmentControllerTelemetryTopic! Error : " + e.getLocalizedMessage());
                    }
                });
            } else {
                logger.error("Mqtt client not connected");
            }
        } catch (Exception e) {
            logger.error("Error subscribing to configuration topic! Error : " + e.getLocalizedMessage());
        }
    }

    /**
     * Function that monitor the topics related with IrrigationController class
     *
     * @param mqttClient    The mqtt client
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void subscribeIrrigationControllerTelemetryTopic(@NotNull IMqttClient mqttClient, DataCollector dataCollector) {
        try {
            int SubscriptionQoS = 0;
            String topicToSubscribe = String.format("%s/%s/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    MqttConfigurationParameters.TELEMETRY_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    try {
                        byte[] payload = msg.getPayload();
                        String payloadString = new String(payload);
//                        logger.info("Message received (" + topic + ") Message Received: " + payloadString);

                        Optional<SenMLPack> parsedPayload = parseSenMLJson(payloadString);

                        if (parsedPayload.isPresent()) {
                            SenMLPack senMLPack = parsedPayload.get();

                            String deviceId = null;
                            Number batteryLevel = null;
                            String batteryUnit = null;
                            Number timestamp = new Date().getTime();
                            for (SenMLRecord record : senMLPack) {
                                String baseName = record.getBn();
                                String name = record.getN();
                                Number value = record.getV();
                                String unit = record.getU();
                                Number time = record.getBt();

                                if (baseName != null) deviceId = baseName;
                                if (name != null && value != null) {
                                    if (name.equals(Battery.SENML_NAME)) {
                                        batteryLevel = value;
                                    }
                                    if (unit != null) batteryUnit = unit;
                                }
                                if (time != null) timestamp = time;
                            }

                            if (deviceId != null && batteryLevel != null) {
                                Optional<IrrigationController> device = dataCollector.getZoneSettings(zoneIdentifier).getSmartObjectById(deviceId, IrrigationController.class);
                                if (device.isPresent()) {
                                    device.get().getBattery().setBatteryPercentage(batteryLevel.intValue());
                                    logDeviceBattery(deviceId, batteryLevel.intValue(), batteryUnit, timestamp.longValue());
                                } else {
                                    logger.error("Error reading subscribeIrrigationControllerTelemetryTopic! missing device: " + deviceId);
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error("Error reading subscribeIrrigationControllerTelemetryTopic! Error : " + e.getLocalizedMessage());
                    }
                });
            } else {
                logger.error("Mqtt client not connected");
            }
        } catch (Exception e) {
            logger.error("Error subscribing to configuration topic! Error : " + e.getLocalizedMessage());
        }
    }


    // TOPICS MESSAGE SENDERS CALLBACKS

    /**
     * Function that sends the new configuration to all clients
     * It sets a retained message, so the new client that connects will have
     * the latest configuration on subscription.
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void sendNewZoneConfigurationToAllLightController(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            logger.info("Sending new configuration for lights!");
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
    public static void sendNewZoneConfigurationToAllIrrigationController(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            logger.info("Sending new configuration for irrigation!");
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
    public static void sendNewZoneConfigurationToAllSmartObjects(@NotNull IMqttClient mqttClient, int zoneId, DataCollector dataCollector) {
        ZoneSettings zoneSettings = dataCollector.getZoneSettings(zoneId);
        if (zoneSettings != null) {
            logger.info("Sending new configuration for all actuators!");
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
    public static void sendNewZoneConfiguration(@NotNull IMqttClient mqttClient, int zoneId, String deviceId, DataCollector dataCollector) {
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
    public static void sendLightControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, LightController lightControllerConfiguration) {
        try {
            int messageQoS = 2;
            boolean retained = true;

            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_LIGHT_TOPIC,
                    deviceId,
                    MqttConfigurationParameters.CONFIGURATION_TOPIC);

            logger.info("Publishing (sendLightControllerConfiguration) to Topic: {}", topic);
            sendPayload(mqttClient, topic, gson.toJson(lightControllerConfiguration), messageQoS, retained);
        } catch (Exception e) {
            logger.error("Error Publishing send new configuration for lightControllers Information! Error : " + e.getLocalizedMessage());
        }
    }

    /**
     * Function that updates the irrigationController configuration of specific irrigationController
     *
     * @param mqttClient           The mqtt client
     * @param deviceId             The device target where publish the configuration
     * @param irrigationController The new irrigationController configuration
     */
    public static void sendIrrigationControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, IrrigationController irrigationController) {
        try {
            int messageQoS = 2;
            boolean retained = true;

            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_IRRIGATION_TOPIC,
                    deviceId,
                    MqttConfigurationParameters.CONFIGURATION_TOPIC);

            logger.info("Publishing (sendIrrigationControllerConfiguration) to Topic: {}", topic);
            sendPayload(mqttClient, topic, gson.toJson(irrigationController), messageQoS, retained);
        } catch (Exception e) {
            logger.error("Error Publishing send new configuration for irrigationController Information! Error : " + e.getLocalizedMessage());
        }
    }


    // UTILS

    /**
     * Logs the device battery of
     *
     * @param deviceId     The device that refers the message
     * @param batteryLevel The battery level
     * @param batteryUnit  The unit of measurement of battery level
     * @param timestamp    When the data was created
     */
    public static void logDeviceBattery(String deviceId, int batteryLevel, String batteryUnit, Long timestamp) {
        if (batteryLevel <= MqttConfigurationParameters.THRESHOLD_BATTERY_PERCENTAGE) {
            logger.warn("[%s] %s: battery level: %d %s, under threshold: %d".formatted(
                    new Date(timestamp).toString(),
                    deviceId,
                    batteryLevel,
                    batteryUnit,
                    MqttConfigurationParameters.THRESHOLD_BATTERY_PERCENTAGE));
        }
    }

    /**
     * Set a new activate status to all irrigationControllers for a given zone
     *
     * @param mqttClient    The mqtt client
     * @param zoneId        The zone where publish the configuration
     * @param active        The new activation status
     * @param dataCollector The data collector object that manages the zones and controllers
     */
    public static void setActivationIrrigationByZone(@NotNull IMqttClient mqttClient, int zoneId, boolean active, DataCollector dataCollector) {
        dataCollector.getZoneSettings(zoneId).getIrrigationControllerConfiguration().getActuator().setActive(active);
        sendNewZoneConfigurationToAllIrrigationController(mqttClient, zoneId, dataCollector);
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
            logger.info("Payload sent -> Topic : {} Payload: {}, retained: {}", topic, payload, retained);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }

    private static void sendPayload(@NotNull IMqttClient mqttClient, String topic, String payload) throws MqttException {
        sendPayload(mqttClient, topic, payload, 0, false);
    }

    private static void sendPayload(@NotNull IMqttClient mqttClient, String topic, String payload, int messageQoS) throws MqttException {
        sendPayload(mqttClient, topic, payload, messageQoS, false);
    }
}
