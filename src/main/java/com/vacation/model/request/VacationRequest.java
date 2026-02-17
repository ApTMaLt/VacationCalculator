package com.vacation.model.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDate;

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

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate vacationDateStart;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate vacationDateEnd;
}