package com.vacation.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VacationRequest {
    @NotNull
    @DecimalMin(value = "0.01", message = "Средняя зарплата должна быть больше 0")
    private BigDecimal averageSalary;

    @NotNull
    @Min(value = 1, message = "Количество дней должно быть не меньше 1")
    private Integer vacationDays;

    //TODO в запросе может быть ещё и дни начала и окончания отпуска
}