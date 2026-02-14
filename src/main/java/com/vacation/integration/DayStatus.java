package com.vacation.integration;

import com.vacation.exception.InvalidVacationRequestException;
import lombok.Getter;

@Getter
public enum DayStatus {
    WORKING_DAY(0, "Рабочий день"),
    NON_WORKING_DAY(1, "Нерабочий день"),
    SHORTENED_DAY(2, "Сокращённый день"),
    WORKING_DAY_SPECIAL(4, "Рабочий день *"),
    HOLIDAY(8, "Праздничный день *"),
    ERROR_INVALID_DATE(100, "Ошибка в дате/коде страны"),
    ERROR_NOT_FOUND(101, "Данные не найдены"),
    ERROR_SERVICE(199, "Ошибка сервиса");

    private final int code;
    private final String description;
    DayStatus(int code, String description) {
        this.code = code;
        this.description = description;
    }

    public boolean isPaidDay() {
        return this == WORKING_DAY || this == SHORTENED_DAY || this == WORKING_DAY_SPECIAL || this == NON_WORKING_DAY;
    }

    public boolean isUnpaidDay() {
        return this == HOLIDAY;
    }

    public static DayStatus fromCode(int code) {
        if (code >= 100) {
            throw new RuntimeException("Ошибка API isdayoff.ru: " + code);
        }
        for (DayStatus status : DayStatus.values()) {
            if (status.getCode() == code) {
                return status;
            }
        }
        throw new InvalidVacationRequestException("Неизвестный код статуса дня");
    }
}
