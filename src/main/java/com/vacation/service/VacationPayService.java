package com.vacation.service;

import com.vacation.exception.InvalidVacationRequestException;
import com.vacation.model.request.VacationRequest;
import com.vacation.model.response.VacationResponse;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class VacationPayService {

    private static final BigDecimal AVERAGE_DAYS_PER_MONTH = new BigDecimal("29.3");

    public VacationResponse calculateVacationPay(VacationRequest vacationRequest) {
        //Валидация запроса
        validateRequest(vacationRequest);

        //Расчет среднего заработка
        BigDecimal dailyAverage = calculateDailyAverage(vacationRequest.getAverageSalary());

        //Количество дней для расчета
        int daysToCalculate = vacationRequest.getVacationDays();

        //Расчет отпускных
        BigDecimal vacationPay = dailyAverage.multiply(BigDecimal.valueOf(daysToCalculate)).setScale(2, RoundingMode.HALF_UP);

        //Формируем ответ
        VacationResponse vacationResponse = new VacationResponse(vacationPay);

        return vacationResponse;
    }

    private BigDecimal calculateDailyAverage(BigDecimal averageSalary) {
        return averageSalary.divide(AVERAGE_DAYS_PER_MONTH,10, RoundingMode.HALF_UP);

    }

    private void validateRequest(VacationRequest vacationRequest) {
        if (vacationRequest.getAverageSalary() == null || vacationRequest.getAverageSalary().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidVacationRequestException("Средняя зарплата должна быть больше 0");
        }
        if (vacationRequest.getVacationDays() == null || vacationRequest.getVacationDays() <= 0){
            throw new InvalidVacationRequestException("Количество дней отпуска должно быть больше 0");
        }
    }


}