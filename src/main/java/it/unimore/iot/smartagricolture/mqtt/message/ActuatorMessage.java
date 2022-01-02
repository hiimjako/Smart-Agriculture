package it.unimore.iot.smartagricolture.mqtt.message;

public class ActuatorMessage<T> {

    private long timestamp;

    private String type;

    private T dataValue;

    public ActuatorMessage() {
    }

    public ActuatorMessage(String type, T dataValue) {
        this.timestamp = System.currentTimeMillis();
        this.type = type;
        this.dataValue = dataValue;
    }

    public ActuatorMessage(long timestamp, String type, T dataValue) {
        this.timestamp = timestamp;
        this.type = type;
        this.dataValue = dataValue;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public T getDataValue() {
        return dataValue;
    }

    public void setDataValue(T dataValue) {
        this.dataValue = dataValue;
    }

}