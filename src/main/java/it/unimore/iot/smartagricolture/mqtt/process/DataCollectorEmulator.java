package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.message.IrrigationControllerConfiguration;
import it.unimore.iot.smartagricolture.mqtt.message.LightControllerConfiguration;
import it.unimore.iot.smartagricolture.mqtt.model.*;
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

public class DataCollectorEmulator {

    private final static Logger logger = LoggerFactory.getLogger(DataCollectorEmulator.class);
    private static final Gson gson = new Gson();
    private static final int zoneIdentifier = 3;

    private static final int zoneIdentifierDemo = 5;
    public static int maxItemInDefaultZoneDemo = 4;
    private static final boolean sendNewConfigurationDemo = false;
    private static final boolean simulateRainAndStopDemo = false;

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
            dataCollector.createZone(zoneIdentifierDemo);

            // Default lightController: Esempio di configurazione custom, con le luci attive
            LightControllerConfiguration defaultLightConfiguration = new LightControllerConfiguration();
            defaultLightConfiguration.getStatus().setValue(true);
            dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultLightConfiguration);
            dataCollector.changeDefaultSettingsZone(zoneIdentifierDemo, defaultLightConfiguration);

            // Default irrigationController: Esempio di configurazione custom
            IrrigationControllerConfiguration defaultIrrigationConfiguration = new IrrigationControllerConfiguration();
            defaultIrrigationConfiguration.getStatus().setValue(true);
            defaultIrrigationConfiguration.getActivationPolicy().setTimeSchedule("01 * * * * *");
            defaultIrrigationConfiguration.getActivationPolicy().setDurationSecond(20);
            defaultIrrigationConfiguration.setIrrigationLevel("medium");
            defaultIrrigationConfiguration.setRotate(false);
            dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);

            defaultIrrigationConfiguration = new IrrigationControllerConfiguration();
            defaultIrrigationConfiguration.getStatus().setValue(true);
            defaultIrrigationConfiguration.getActivationPolicy().setTimeSchedule("* * 01 * * *");
            defaultIrrigationConfiguration.getActivationPolicy().setDurationMinute(20);
            defaultIrrigationConfiguration.setIrrigationLevel("medium");
            defaultIrrigationConfiguration.setRotate(false);
            dataCollector.changeDefaultSettingsZone(zoneIdentifierDemo, defaultIrrigationConfiguration);

            // Subscribing
            subscribePresentationTopic(mqttClient, dataCollector);
            subscribeIrrigationControllerTelemetryTopic(mqttClient, dataCollector);
            subscribeEnvironmentControllerTelemetryTopic(mqttClient, dataCollector);

            // Send as retained the default configurations -> reset retained with default
            // sendNewZoneConfigurationToAllSmartObjects(mqttClient, zoneIdentifier, dataCollector);

            if (sendNewConfigurationDemo) {
                // simulazione di cambio configurazione dopo 10 secondi
                Thread.sleep(10000);
                defaultLightConfiguration.getStatus().setValue(false);

                dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultLightConfiguration);
                sendNewZoneConfigurationToAllLightController(mqttClient, zoneIdentifier, dataCollector);

                Thread.sleep(10000);
                defaultIrrigationConfiguration.getActivationPolicy().setDurationMinute(1);
                defaultIrrigationConfiguration.setIrrigationLevel("low");
                defaultIrrigationConfiguration.setRotate(true);

                dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);
                sendNewZoneConfigurationToAllIrrigationController(mqttClient, zoneIdentifier, dataCollector);
            }

            if (simulateRainAndStopDemo) {
                // simulazione di cambio configurazione dopo 10 secondi
                Thread.sleep(3000);
                defaultIrrigationConfiguration.getActivationPolicy().setTimeSchedule("01 * * * * *");
                defaultIrrigationConfiguration.getStatus().setValue(true);
                defaultIrrigationConfiguration.getActivationPolicy().setDurationMinute(10);
                defaultIrrigationConfiguration.setIrrigationLevel("low");
                defaultIrrigationConfiguration.setRotate(true);
                dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);
                sendNewZoneConfigurationToAllIrrigationController(mqttClient, zoneIdentifier, dataCollector);

                Thread.sleep(20000);
                defaultIrrigationConfiguration.getStatus().setValue(false);
                dataCollector.changeDefaultSettingsZone(zoneIdentifier, defaultIrrigationConfiguration);
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
            int SubscriptionQoS = 2;
            String topicToSubscribe = String.format("%s/+/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.PRESENTATION_TOPIC);

            if (mqttClient.isConnected()) {
                logger.info("Subscribed to topic: (" + topicToSubscribe + ")");
                mqttClient.subscribe(topicToSubscribe, SubscriptionQoS, (topic, msg) -> {
                    byte[] payload = msg.getPayload();
                    String payloadString = new String(payload);
                    SmartObjectBase deviceInfo = gson.fromJson(new String(msg.getPayload()), SmartObjectBase.class);
                    String deviceType = deviceInfo.getDeviceType();
                    int zoneId = zoneIdentifier;

                    if (dataCollector.getZoneSettings(zoneId).getAllSmartObjectIds().size() >= maxItemInDefaultZoneDemo) {
                        zoneId = zoneIdentifierDemo;
                    }

                    logger.info("subscribePresentationTopic -> Message Received (" + topic + ") Message Received: " + payloadString);

                    // FIXME: mettere la zona dinamica, ora sempre questa fissa
                    switch (deviceType) {
                        case LightController.DEVICE_TYPE -> dataCollector.addSmartObjectToZone(zoneId, gson.fromJson(payloadString, LightController.class));
                        case IrrigationController.DEVICE_TYPE -> dataCollector.addSmartObjectToZone(zoneId, gson.fromJson(payloadString, IrrigationController.class));
                        case EnvironmentalSensor.DEVICE_TYPE -> dataCollector.addSmartObjectToZone(zoneId, gson.fromJson(payloadString, EnvironmentalSensor.class));
                        default -> logger.error("subscribePresentationTopic -> Unable to convert object: {}", payloadString);
                    }

                    if (!msg.isRetained()) {
                        sendNewZoneConfiguration(mqttClient, zoneId, deviceInfo.getId(), dataCollector);
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
                                            isTemperatureUnderThreshold = Temperature.isUnderTemperature(value.doubleValue());
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
                            if (deviceId != null) {
                                int deviceZone = dataCollector.getDeviceZone(deviceId);
                                Optional<EnvironmentalSensor> device = dataCollector.getZoneSettings(deviceZone).getSmartObjectById(deviceId, EnvironmentalSensor.class);
                                if (device.isPresent()) {
                                    if (batteryLevel != null) {
                                        device.get().getBattery().setValue(batteryLevel.intValue());
                                        logDeviceBattery(deviceId, batteryLevel.intValue(), batteryUnit, timestamp.longValue());
                                    }
                                } else {
                                    logger.error("Error reading subscribeEnvironmentControllerTelemetryTopic! missing device: " + deviceId);
                                }

                                boolean shouldStopIrrigation = isRaining || isTemperatureUnderThreshold;
                                boolean currentStatus = dataCollector.getZoneSettings(deviceZone).getIrrigationControllerConfiguration().getStatus().getValue();
                                // invio la nuova configurazione solo se cambia da quella precedente

                                if (shouldStopIrrigation) {
                                    if (currentStatus) {
                                        logger.info("Detected environmental change -> isRaining: {}, isTemperatureUnderThreshold: {}, currentIrrigationStatus: {}",
                                                isRaining,
                                                isTemperatureUnderThreshold,
                                                currentStatus
                                        );
                                        setActivationIrrigationByZone(mqttClient, deviceZone, false, dataCollector);
                                    }
                                } else {
                                    if (!currentStatus) {
                                        logger.info("Detected environmental change -> isRaining: {}, isTemperatureUnderThreshold: {}, currentIrrigationStatus: {}",
                                                isRaining,
                                                isTemperatureUnderThreshold,
                                                currentStatus
                                        );
                                        setActivationIrrigationByZone(mqttClient, deviceZone, true, dataCollector);
                                    }
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

                            if (deviceId != null) {
                                int deviceZone = dataCollector.getDeviceZone(deviceId);
                                Optional<IrrigationController> device = dataCollector.getZoneSettings(deviceZone).getSmartObjectById(deviceId, IrrigationController.class);
                                if (device.isPresent()) {
                                    if (batteryLevel != null) {
                                        device.get().getBattery().setValue(batteryLevel.intValue());
                                        logDeviceBattery(deviceId, batteryLevel.intValue(), batteryUnit, timestamp.longValue());
                                    }
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
            for (String deviceId : zoneSettings.getAllSmartObjectIds()) {
                Optional<SmartObjectBase> object = zoneSettings.getSmartObjectById(deviceId);
                if (object.isPresent()) {
                    if (object.get() instanceof LightController)
                        sendNewZoneConfiguration(mqttClient, zoneId, deviceId, dataCollector);
                }
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
            for (String deviceId : zoneSettings.getAllSmartObjectIds()) {
                Optional<SmartObjectBase> object = zoneSettings.getSmartObjectById(deviceId);
                if (object.isPresent()) {
                    if (object.get() instanceof IrrigationController)
                        sendNewZoneConfiguration(mqttClient, zoneId, deviceId, dataCollector);
                }
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
            for (String deviceId : zoneSettings.getAllSmartObjectIds()) {
                sendNewZoneConfiguration(mqttClient, zoneId, deviceId, dataCollector);
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
    public static void sendLightControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, LightControllerConfiguration lightControllerConfiguration) {
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
    public static void sendIrrigationControllerConfiguration(@NotNull IMqttClient mqttClient, String deviceId, IrrigationControllerConfiguration irrigationController) {
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
        dataCollector.getZoneSettings(zoneId).getIrrigationControllerConfiguration().getStatus().setValue(active);
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
