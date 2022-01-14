package it.unimore.iot.smartagricolture.mqtt.model.actuator;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

public class Time {
    private String timeSchedule = ""; //"* * * * * *";
    private int durationHour = 0;
    private int durationMinute = 0;
    private long lastRun;

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

    public String getTimeSchedule() {
        return timeSchedule;
    }

    public void setTimeSchedule(String timeSchedule) {
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

    public long getLastRun() {
        return lastRun;
    }

    public void setLastRun(long lastRun) {
        this.lastRun = lastRun;
    }

    public Date getNextDateToActivate() {
        CronSequenceGenerator generator = new CronSequenceGenerator(this.timeSchedule);
        return generator.next(new Date());
    }

    public boolean hasToStop() {
        long previousRun = this.getLastRun();
        previousRun += (long) this.durationHour * HOUR;
        previousRun += (long) this.durationMinute * MINUTE;

        long now = new Date().getTime();
        return now < previousRun;
    }

    @Override
    public String toString() {
        return "Time{" +
                "timeSchedule='" + timeSchedule + '\'' +
                ", durationHour=" + durationHour +
                ", durationMinute=" + durationMinute +
                ", lastRun=" + lastRun +
                '}';
    }
}
