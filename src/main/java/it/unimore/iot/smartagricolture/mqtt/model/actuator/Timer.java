package it.unimore.iot.smartagricolture.mqtt.model.actuator;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class Timer {
    // default al primo giorno dell'anno
    private String timeSchedule = "0 0 0 1 1 0"; //"sec min hour day(month) month day(week)";
    private int durationHour = 0;
    private int durationMinute = 0;
    private int durationSecond = 0;
    private long lastRunStartTimestamp;

    public static final long SECOND = 1000; // in milli-seconds.
    public static final long MINUTE = 60 * 1000; // in milli-seconds.
    public static final long HOUR = 3600 * 1000; // in milli-seconds.

    public Timer() {
    }

    public Timer(String timeSchedule, int durationHour, int durationMinute, int durationSecond) {
        if (!CronSequenceGenerator.isValidExpression(timeSchedule))
            throw new IllegalArgumentException(String.format("Cron expression must consist of 6 fields (found %d in \"%s\")",
                    timeSchedule.split(" ").length, timeSchedule));

        this.timeSchedule = timeSchedule;
        this.durationHour = durationHour;
        this.durationMinute = durationMinute;
        this.durationSecond = durationSecond;
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

    public int getDurationSecond() {
        return durationSecond;
    }

    public void setDurationSecond(int durationSecond) {
        this.durationSecond = durationSecond;
    }

    public long getLastRunStartTimestamp() {
        return lastRunStartTimestamp;
    }

    public void setLastRunStart() {
        this.lastRunStartTimestamp = new Date().getTime();
    }

    public void setLastRunStartTimestamp(long lastRunStartTimestamp) {
        this.lastRunStartTimestamp = lastRunStartTimestamp;
    }

    public Date getNextDateToActivate() {
        CronSequenceGenerator generator = new CronSequenceGenerator(this.timeSchedule);
        return generator.next(new Date());
    }

    public long dateWhenFinishRun() {
        long run = this.getLastRunStartTimestamp();
        run += (long) this.durationHour * HOUR;
        run += (long) this.durationMinute * MINUTE;
        run += (long) this.durationSecond * SECOND;

        return run;
    }

    public boolean hasToStop() {
        long previousRun = this.dateWhenFinishRun();
        return new Date().after(new Date(previousRun));
    }

    public long milliSecondsUntilEnd() {
        long previousRun = this.dateWhenFinishRun();
        return previousRun - new Date().getTime();
    }


    @Override
    public String toString() {
        return "Time{" +
                "timeSchedule='" + timeSchedule + '\'' +
                ", durationHour=" + durationHour +
                ", durationMinute=" + durationMinute +
                ", lastRun=" + lastRunStartTimestamp +
                '}';
    }
}
