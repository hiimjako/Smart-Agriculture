package it.unimore.iot.smartagricolture.mqtt.model.actuator;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

public class Time {
    private String timeSchedule = ""; //"* * * * * *";
    private int durationHour = 0;
    private int durationMinute = 0;

    public static final long MINUTE = 60 * 1000; // in milli-seconds.
    public static final long HOUR = 3600 * 1000; // in milli-seconds.

    public Time() {
    }

    public Time(String timeSchedule, int durationHour, int durationMinute) {
        if (!CronSequenceGenerator.isValidExpression(timeSchedule))
            throw new IllegalArgumentException(String.format("Cron expression must consist of 6 fields (found %d in \"%s\")",
                    timeSchedule.split(" ").length, timeSchedule));

        this.timeSchedule = timeSchedule;
        this.durationHour = durationHour;
        this.durationMinute = durationMinute;
    }

    public String getTimePolicy() {
        return timeSchedule;
    }

    public void setTimePolicy(String timeSchedule) {
        this.timeSchedule = timeSchedule;
    }

    public int getDurationHour() {
        return durationHour;
    }

    public void setDurationHour(int durationHour) {
        this.durationHour = durationHour;
    }

    public int getDurationMinute() {
        return durationMinute;
    }

    public void setDurationMinute(int durationMinute) {
        this.durationMinute = durationMinute;
    }

    public Date getNextDateToActivate() {
        CronSequenceGenerator generator = new CronSequenceGenerator(this.timeSchedule);
        return generator.next(new Date());
    }

    public boolean hasToFinish() {
        long nextDate = this.getNextDateToActivate().getTime();
        long now = new Date().getTime();
        now += (long) this.durationHour * HOUR;
        now += (long) this.durationMinute * MINUTE;
        return now < nextDate;
    }

    @Override
    public String toString() {
        return "Time{" +
                "timeSchedule='" + timeSchedule + '\'' +
                ", durationHour=" + durationHour +
                ", durationMinute=" + durationMinute +
                '}';
    }
}
