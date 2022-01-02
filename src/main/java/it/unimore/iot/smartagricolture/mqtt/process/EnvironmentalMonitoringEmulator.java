package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.EnvironmentalSensor;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EnvironmentalMonitoringEmulator {
    private final static Logger logger = LoggerFactory.getLogger(EnvironmentalMonitoringEmulator.class);
    private final static int BATTERY_DRAIN = 15;

    public static void main(String[] args) {
        try {

            String zoneId = "1";
            EnvironmentalSensor environmentalSensor = new EnvironmentalSensor(zoneId);

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    MqttConfigurationParameters.BROKER_URL,
                    environmentalSensor.getId(),
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            logger.info("Connected!");

            publishDeviceInfo(mqttClient, environmentalSensor);

            while (environmentalSensor.getBattery().getBatteryPercentage() > 0) {
                logger.info("Current battery level: {}", environmentalSensor.getBattery().getBatteryPercentage());
                if (environmentalSensor.getBattery().isBatteryUnderThreshold()) {
                    publishDeviceBattery(mqttClient, environmentalSensor);
                }
                publishTelemetryData(mqttClient, environmentalSensor);
                // simulazione scaricamento della batteria
                environmentalSensor.getBattery().decreaseBatteryLevelBy(BATTERY_DRAIN);
                Thread.sleep(2000);
            }

            mqttClient.disconnect();
            mqttClient.close();
            logger.info("Disconnected!");

        } catch (Exception e) {
            logger.error("Error while publishing!");
            e.printStackTrace();
        }
    }

    /**
     * Send the current battery percentage to the specified MQTT topic
     *
     * @param mqttClient          The mqtt client
     * @param environmentalSensor Instance of environmentalSensor, to get ids and battery status
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishDeviceBattery(IMqttClient mqttClient, EnvironmentalSensor environmentalSensor) throws MqttException {
        String topic = String.format("%s/%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                environmentalSensor.getZoneId(),
                MqttConfigurationParameters.ENV_SENSOR_TOPIC,
                environmentalSensor.getId(),
                MqttConfigurationParameters.BATTERY_PERCENTAGE_TOPIC);

        //TODO: only sensors data, not also id and so on
        Gson gson = new Gson();
        String payloadString = gson.toJson(environmentalSensor.getBattery().getBatteryPercentage());
        logger.info("Publishing (publishDeviceBattery) to Topic: {} Data: {}", topic, payloadString);

        if (mqttClient.isConnected() && payloadString != null && topic != null) {
            MqttMessage msg = new MqttMessage(payloadString.getBytes());
            msg.setQos(0);
            mqttClient.publish(topic, msg);

            logger.info("Battery status sent -> Topic : {} Payload: {}", topic, payloadString);
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }

    /**
     * Send the sensor infos Payload to the specified MQTT topic
     *
     * @param mqttClient          The mqtt client
     * @param environmentalSensor Instance of environmentalSensor, to get ids and battery status
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, EnvironmentalSensor environmentalSensor) throws MqttException {
        String topic = String.format("%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                environmentalSensor.getZoneId(),
                MqttConfigurationParameters.ENV_SENSOR_TOPIC,
                environmentalSensor.getId()
        );

        Gson gson = new Gson();
        String payloadString = gson.toJson(environmentalSensor);

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
     * Send the sensor telemetry data as Payload to the specified MQTT topic
     *
     * @param mqttClient          The mqtt client
     * @param environmentalSensor Instance of environmentalSensor, to get ids and battery status
     * @throws MqttException Error thrown by publish method of mqtt client
     */
    public static void publishTelemetryData(IMqttClient mqttClient, EnvironmentalSensor environmentalSensor) throws MqttException {
        Gson gson = new Gson();
        String topic = String.format("%s/%s/%s/%s/%s/%s",
                MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                MqttConfigurationParameters.ZONE_TOPIC,
                environmentalSensor.getZoneId(),
                MqttConfigurationParameters.ENV_SENSOR_TOPIC,
                environmentalSensor.getId(),
                MqttConfigurationParameters.TELEMETRY_TOPIC);

        //TODO: only sensors data, not also id and so on
        String payloadString = gson.toJson(environmentalSensor);

        logger.info("Publishing (publishTelemetryData) to Topic: {} Data: {}", topic, payloadString);
        if (mqttClient.isConnected() && payloadString != null && topic != null) {
            MqttMessage msg = new MqttMessage(payloadString.getBytes());
            msg.setQos(0);
            msg.setRetained(true);
            mqttClient.publish(topic, msg);
            logger.info("Telemetry data correctly published!");
        } else {
            logger.error("Error: Topic or Msg = Null or MQTT Client is not Connected!");
        }
    }
}
