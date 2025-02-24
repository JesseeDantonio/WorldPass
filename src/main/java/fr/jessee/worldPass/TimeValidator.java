package fr.jessee.worldPass;

import java.time.LocalTime;

public class TimeValidator {

    public boolean isWithinTimeRange(LocalTime startTime, LocalTime endTime) {
        LocalTime now = LocalTime.now();
        return now.isAfter(startTime) && now.isBefore(endTime);
    }
}
