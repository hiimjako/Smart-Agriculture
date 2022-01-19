package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.EnvironmentalSensor;
import it.unimore.iot.smartagricolture.mqtt.utils.SenMLPack;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.Random;

import static it.unimore.iot.smartagricolture.mqtt.utils.SenMLParser.toSenMLJson;

public class EnvironmentalMonitoringEmulator {
    private static final int BATTERY_DRAIN = 2;
    private static final int BATTERY_DRAIN_TICK_PERIOD = 10000;
    private static final boolean SIMULATE_RAIN = true;

    private final static Logger logger = LoggerFactory.getLogger(EnvironmentalMonitoringEmulator.class);
    private static final Gson gson = new Gson();

    public static void main(String[] args) {
        try {

            EnvironmentalSensor environmentalSensor = new EnvironmentalSensor();
            environmentalSensor.setId("test-env-1234");
            environmentalSensor.getBattery().setValue(50);
            environmentalSensor.getRainSensor().setValue(false);
            environmentalSensor.getTemperatureSensor().setValue(MqttConfigurationParameters.THRESHOLD_TEMPERATURE_CEL + 1);


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

            Random rand = new Random();
            rand.setSeed(System.currentTimeMillis());

            environmentalSensor.getRainSensor().setValue(false);
            while (environmentalSensor.getBattery().getValue() > 0) {
                if (SIMULATE_RAIN) {
                    environmentalSensor.getTemperatureSensor().setValue(rand.nextDouble(-10, 50));
                    environmentalSensor.getRainSensor().setValue(rand.nextBoolean());
                }
                environmentalSensor.getBattery().decreaseBatteryLevelBy(BATTERY_DRAIN);
                environmentalSensor.getHumiditySensor().setValue(rand.nextDouble(0, 100));
                environmentalSensor.getBrightnessSensor().setValue(rand.nextDouble(0, 100));
                publishDeviceTelemetry(mqttClient, environmentalSensor);

                Thread.sleep(BATTERY_DRAIN_TICK_PERIOD);
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
     * Send the sensor infos Payload to the specified MQTT topic
     *
     * @param mqttClient          The mqtt client
     * @param environmentalSensor Instance of EnvironmentalSensor, to get ids and battery status
     */
    public static void publishDeviceInfo(IMqttClient mqttClient, EnvironmentalSensor environmentalSensor) {
        try {
            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC,
                    environmentalSensor.getId(),
                    MqttConfigurationParameters.PRESENTATION_TOPIC);

            String payloadString = gson.toJson(environmentalSensor);
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
            logger.error("Error Publishing EnvironmentalSensor Information! Error : " + e.getLocalizedMessage());
        }
    }

    /**
     * Send the sensor sensors values Payload to the specified MQTT topic in SenML format
     *
     * @param mqttClient          The mqtt client
     * @param environmentalSensor Instance of EnvironmentalSensor, to get ids and battery status
     */
    public static void publishDeviceTelemetry(IMqttClient mqttClient, EnvironmentalSensor environmentalSensor) {
        try {
            String topic = String.format("%s/%s/%s/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.SM_OBJECT_ENVIRONMENTAL_TOPIC,
                    environmentalSensor.getId(),
                    MqttConfigurationParameters.TELEMETRY_TOPIC);

            SenMLPack senml = environmentalSensor.toSenML();
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
}
