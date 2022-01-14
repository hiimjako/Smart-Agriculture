package it.unimore.iot.smartagricolture.mqtt.model.actuator;

import org.springframework.scheduling.support.CronSequenceGenerator;

import java.util.Date;

/**
 * @author Alberto Moretti, 272804@studenti.unimore.it
 * @version 1.0.0
 * @project smart-agriculture
 * @created 14/01/2022 - 13:11
 */
public class Time {
    // default al primo giorno dell'anno
    private String timeSchedule = "0 0 0 1 1 0"; //"sec min hour day(month) month day(week)";
    private int durationHour = 0;
    private int durationMinute = 0;
    private long lastRunStart;

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

    public long getLastRunStart() {
        return lastRunStart;
    }

    public void setLastRunStart() {
        this.lastRunStart = new Date().getTime();
    }

    public void setLastRunStart(long lastRunStart) {
        this.lastRunStart = lastRunStart;
    }

    public Date getNextDateToActivate() {
        CronSequenceGenerator generator = new CronSequenceGenerator(this.timeSchedule);
        return generator.next(new Date());
    }

    public boolean hasToStop() {
        long previousRun = this.getLastRunStart();
        previousRun += (long) this.durationHour * HOUR;
        previousRun += (long) this.durationMinute * MINUTE;

        return new Date().after(new Date(previousRun));
    }

    public long milliSecondsUntilEnd() {
        long previousRun = this.getLastRunStart();
        previousRun += (long) this.durationHour * HOUR;
        previousRun += (long) this.durationMinute * MINUTE;

        return previousRun - new Date().getTime();
    }


    @Override
    public String toString() {
        return "Time{" +
                "timeSchedule='" + timeSchedule + '\'' +
                ", durationHour=" + durationHour +
                ", durationMinute=" + durationMinute +
                ", lastRun=" + lastRunStart +
                '}';
    }
}
