package com.vacation.integration;

import lombok.Getter;

import java.time.LocalDate;

@Getter
public class DayInfo {
    private final LocalDate date;
    private final DayStatus status;

    private final boolean isPaidDay;
    private final boolean isUnpaidDay;

    public DayInfo(LocalDate date, DayStatus status) {
        this.date = date;
        this.status = status;
        this.isUnpaidDay = status.isUnpaidDay();
        this.isPaidDay = status.isPaidDay();
    }

    @Override
    public String toString() {
        return String.format("%s: %s", date, status.getDescription());
    }
}


