package com.ermetic.dosserver.services;

import java.time.Instant;
import java.util.Objects;


/**
 * Immutable object representing a time frame
 */
public class ClientTimeFrame {
    private final Instant startTime;
    private final Integer requestsCount;

    public ClientTimeFrame(Instant startTime, Integer requestsCount) {
        this.startTime = startTime;
        this.requestsCount = requestsCount;
    }

    public ClientTimeFrame increaseCounter() {
        return new ClientTimeFrame(startTime, requestsCount + 1);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientTimeFrame timeFrame = (ClientTimeFrame) o;
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
}
