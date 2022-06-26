package com.ermetic.dosserver.services.time_frame;

import java.time.Instant;
import java.util.Objects;


/**
 * Immutable object representing a time frame
 */
public class TimeFrame {
    private final Instant startTime;
    private final Integer requestsCount;

    public TimeFrame(Instant startTime, Integer requestsCount) {
        this.startTime = startTime;
        this.requestsCount = requestsCount;
    }

    public TimeFrame increaseCounter() {
        return new TimeFrame(startTime, requestsCount + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TimeFrame timeFrame = (TimeFrame) o;
        return startTime.equals(timeFrame.startTime);
    }

    @Override
    public int hashCode() {
        return Objects.hash(startTime);
    }

    public Instant getStartTime() {
        return startTime;
    }

    public Integer getRequestsCount() {
        return requestsCount;
    }

    @Override
    public String toString() {
        return "ClientTimeFrame{" +
                "startTime=" + startTime +
                ", requestsCount=" + requestsCount +
                '}';
    }
}
