package it.unimore.iot.smartagricolture.mqtt.process;

import it.unimore.iot.smartagricolture.mqtt.conf.MqttConfigurationParameters;
import org.eclipse.paho.client.mqttv3.IMqttClient;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttClientPersistence;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.UUID;

public class DataCollectorEmulator {


    public static void main(String[] args) {
        try {

            String clientId = UUID.randomUUID().toString();

            MqttClientPersistence persistence = new MemoryPersistence();
            IMqttClient mqttClient = new MqttClient(
                    String.format("tcp://%s:%d", MqttConfigurationParameters.BROKER_ADDRESS, MqttConfigurationParameters.BROKER_PORT),
                    clientId,
                    persistence);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName(MqttConfigurationParameters.MQTT_USERNAME);
            options.setPassword(new String(MqttConfigurationParameters.MQTT_PASSWORD).toCharArray());
            options.setAutomaticReconnect(true);
            options.setCleanSession(true);
            options.setConnectionTimeout(10);

            mqttClient.connect(options);

            System.out.println("Connected!");

            String topic_subscribe = String.format("%s/%s/+/%s/+",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    MqttConfigurationParameters.LIGHT_TOPIC);

            //Subscribe to the target topic #. In that case the consumer will receive (if
            //passing through the broker
            mqttClient.subscribe(topic_subscribe, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the receive
                //messaged from multiple and different topic can be received with the sam
                //The msg variable is a MqttMessage object containing all the information
                byte[] payload = msg.getPayload();
                System.out.println("Message Received (" + topic + ") Message Received: " + new String(payload));

            });

            String topic_subscribe2 = String.format("%s/%s/+/%s/+/%s",
                    MqttConfigurationParameters.MQTT_BASIC_TOPIC,
                    MqttConfigurationParameters.ZONE_TOPIC,
                    MqttConfigurationParameters.LIGHT_TOPIC,
                    MqttConfigurationParameters.ACTUATOR_STATUS_TOPIC);

            //Subscribe to the target topic #. In that case the consumer will receive (if
            //passing through the broker
            mqttClient.subscribe(topic_subscribe2, (topic, msg) -> {
                //The topic variable contain the specific topic associated to the receive
                //messaged from multiple and different topic can be received with the sam
                //The msg variable is a MqttMessage object containing all the information
                byte[] payload = msg.getPayload();
                System.out.println("Message Received (" + topic + ") Message Received: " + new String(payload));

            });


//            mqttClient.disconnect();
//            mqttClient.close();
            System.out.println(" Disconnected !");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
