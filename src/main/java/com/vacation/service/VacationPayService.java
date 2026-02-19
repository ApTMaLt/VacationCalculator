package com.vacation.service;

import com.vacation.exception.InvalidVacationRequestException;
import com.vacation.integration.IsDayOffClient;
import com.vacation.model.request.VacationRequest;
import com.vacation.model.response.VacationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.temporal.ChronoUnit;

@Service
public class VacationPayService {

    private static final BigDecimal AVERAGE_DAYS_PER_MONTH = new BigDecimal("29.3");

    private final IsDayOffClient isDayOffClient;

    public VacationPayService(IsDayOffClient isDayOffClient) {
        this.isDayOffClient = isDayOffClient;
    }

    public VacationResponse calculateVacationPay(VacationRequest vacationRequest) {
        //Валидация запроса
        validateRequest(vacationRequest);

        //Расчет среднего заработка
        BigDecimal dailyAverage = calculateDailyAverage(vacationRequest.getAverageSalary());

        //Количество дней для расчета
        int daysToCalculate = calculateDays(vacationRequest);

        //Расчет отпускных
        BigDecimal vacationPay = dailyAverage.multiply(BigDecimal.valueOf(daysToCalculate)).setScale(2, RoundingMode.HALF_UP);

        return new VacationResponse(vacationPay);
    }

    private int calculateDays(VacationRequest request) {
        if (request.getVacationDateStart() != null && request.getVacationDateEnd() != null) {
            int days = isDayOffClient.countPaidDays(request.getVacationDateStart(), request.getVacationDateEnd());
            if (days <= 0) {
                throw new IllegalArgumentException("Отпуск не может состоять только из праздников");
            }
            return days;
        } else {
            return request.getVacationDays();
        }
    }

    private BigDecimal calculateDailyAverage(BigDecimal averageSalary) {
        return averageSalary.divide(AVERAGE_DAYS_PER_MONTH, 10, RoundingMode.HALF_UP);

    }

    private void validateRequest(VacationRequest vacationRequest) {
        if (vacationRequest.getAverageSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidVacationRequestException("Средняя зарплата должна быть больше 0");
        }
        if (vacationRequest.getVacationDays() <= 0) {
            throw new InvalidVacationRequestException("Количество дней отпуска должно быть больше 0");
        }
        if (vacationRequest.getVacationDateStart() != null && vacationRequest.getVacationDateEnd() != null) {
            if (!vacationRequest.getVacationDateStart().isBefore(vacationRequest.getVacationDateEnd())) {
                throw new InvalidVacationRequestException("Дата окончания отпуска должна быть после даты начала");
            }
            long actualDays = ChronoUnit.DAYS.between(vacationRequest.getVacationDateStart(), vacationRequest.getVacationDateEnd()) + 1;
            if (actualDays != vacationRequest.getVacationDays()) {
                throw new InvalidVacationRequestException("Несоответствие количества дней и дат");
            }
        }
    }

}