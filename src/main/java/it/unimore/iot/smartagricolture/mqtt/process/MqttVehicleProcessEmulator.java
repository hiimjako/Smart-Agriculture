package it.unimore.iot.smartagricolture.mqtt.process;

import com.google.gson.Gson;
import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import it.unimore.iot.smartagricolture.mqtt.model.LightController;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

public class MqttVehicleProcessEmulator {
    private static final int MESSAGE_COUNT = 1000;

    public static void main(String[] args) {
        try {

            String vehicleId = String.format("vehicle-%s", MqttConfigurationParameters.MQTT_USERNAME);

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    String.format("tcp://%s:%d", MqttConfigurationParameters.BROKER_ADDRESS, MqttConfigurationParameters.BROKER_PORT),
                    vehicleId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            System.out.println("Connected!");

            Integer zoneId = 1;
            LightController lightController = new LightController(true);

            publishDeviceInfo(mqttClient, zoneId, lightController);

            for (int i = 0; i < MESSAGE_COUNT; i++) {
                lightController.setActive(!lightController.isActive());
                publishTelemetryData(mqttClient, zoneId, lightController.getId(), lightController);
                Thread.sleep(3000);
            }

            mqttClient.disconnect();
            mqttClient.close();
            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void publishDeviceInfo(IMqttClient mqttClient, Integer zoneId, LightController lightDescriptor) {
        try {
            Gson gson = new Gson();
            if (mqttClient.isConnected()) {
                String topic = String.format("%s/%s/%d/%s/%d/%s",
                        MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                        MqttConfigurationParameters.ZONE_TOPIC,
                        zoneId,
                        MqttConfigurationParameters.LIGHT_TOPIC,
                        lightDescriptor.getId(),
                        MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

                String payloadString = gson.toJson(lightDescriptor);
                MqttMessage msg = new MqttMessage(payloadString.getBytes());
                msg.setQos(0);
                msg.setRetained(true);
                mqttClient.publish(topic, msg);

                System.out.println(" Device Data Correctly Published ! Topic : " + topic + " Payload:" + payloadString);
            } else {
                System.err.println(" Error : Topic or Msg = Null or MQTT Client is not Connected !");
            }
        } catch (Exception e) {
            System.err.println(" Error Publishing Vehicle Information ! Error : " + e.getLocalizedMessage());
        }

    }

    public static void publishTelemetryData(IMqttClient mqttClient, Integer zoneId, Integer vehicleId, LightController telemetryData) {

        try {

            Gson gson = new Gson();

            String topic = String.format("%s/%s/%d/%s/%d/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    zoneId,
                    MqttConfigurationParameters.LIGHT_TOPIC,
                    vehicleId,
                    MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

            String payloadString = gson.toJson(telemetryData.isActive());

            System.out.println(" Publishing to Topic : " + topic + " Data : " + payloadString);

            if (mqttClient.isConnected() && payloadString != null && topic != null) {

                MqttMessage msg = new MqttMessage(payloadString.getBytes());
                msg.setQos(0);
                msg.setRetained(true);
                mqttClient.publish(topic, msg);
                System.out.println(" Data Correctly Published !");
            } else {
                System.err.println(" Error : Topic or Msg = Null or MQTT Client is not Connected!");
            }
        } catch (Exception e) {
            System.err.println(" Error Publishing Telemetry Information ! Error : " + e.getLocalizedMessage());
        }

    }
}