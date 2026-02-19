package com.example.VacationCalculator.service;

import com.vacation.exception.InvalidVacationRequestException;
import com.vacation.integration.IsDayOffClient;
import com.vacation.model.request.VacationRequest;
import com.vacation.model.response.VacationResponse;
import com.vacation.service.VacationPayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.time.LocalDate;

@ExtendWith(MockitoExtension.class)
class VacationPayServiceTest {
    @Mock
    private IsDayOffClient isDayOffClient;

    private VacationPayService vacationPayService;

    @BeforeEach
    void setUp() {
        vacationPayService = new VacationPayService(isDayOffClient);
    }

    @Test
    void calculateVacationPay_withValidBasicRequest_shouldReturnCorrectAmount(){
        // Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 28, null, null);

        //When
        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("9556.31"));
    }

    @Test
    void calculateVacationPay_withMinimumSalaryAndDays_shouldReturnCorrectAmount(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("0.01"), 1, null, null);

        //When
        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("0"));
    }

    @Test
    void calculateVacationPay_withLargeSalaryAndDays_shouldReturnCorrectAmount(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100000"), 365, null, null);

        //When
        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("1245733.79"));
    }

    @Test
    void calculateVacationPay_withZeroSalary_shouldThrowException(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(BigDecimal.ZERO, 1, null, null);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Средняя зарплата должна быть больше 0");
    }

    @Test
    void calculateVacationPay_withNegativeSalary_shouldThrowException(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("-100"), 1, null, null);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Средняя зарплата должна быть больше 0");
    }

    @Test
    void calculateVacationPay_withZeroDays_shouldThrowException(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100"), 0, null, null);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Количество дней отпуска должно быть больше 0");
    }

    @Test
    void calculateVacationPay_withNegativeDays_shouldThrowException(){
        //Given
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100"), -10, null, null);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Количество дней отпуска должно быть больше 0");
    }


    @Test
    void calculateVacationPay_withEndDateBeforeStart_shouldThrowException(){
        //Given
        LocalDate startDate = LocalDate.of(2026, 1, 10);
        LocalDate endDate = LocalDate.of(2026, 1, 1);

        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100"), 10, startDate, endDate);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Дата окончания отпуска должна быть после даты начала");
    }

    @Test
    void calculateVacationPay_withEqualsDates_shouldThrowException(){
        //Given
        LocalDate date = LocalDate.of(2026, 1, 1);

        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100"), 10, date, date);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Дата окончания отпуска должна быть после даты начала");
    }

    @Test
    void calculateVacationPay_withDateRangeNotMatchingDays_shouldThrowException(){
        //Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 2);

        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("100"), 10, startDate, endDate);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Несоответствие количества дней и дат");
    }

    @Test
    void calculateVacationPay_withValidDatesRangeAndNoHolidays_shouldReturnCorrectAmount(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 6, 1);
        LocalDate endDate = LocalDate.of(2026, 6, 28);
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 28, startDate, endDate);

        //When
        when(isDayOffClient.countPaidDays(startDate, endDate)).thenReturn(28);

        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("9556.31"));
    }

    @Test
    void calculateVacationPay_withHolidaysInDatesRange_shouldReturnCorrectAmount(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 22);
        LocalDate endDate = LocalDate.of(2026, 2, 23);
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 2, startDate, endDate);

        //When
        when(isDayOffClient.countPaidDays(startDate, endDate)).thenReturn(1);

        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("341.3"));
    }

    @Test
    void calculateVacationPay_withWeekendInDatesRange_shouldReturnCorrectAmount(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 21);
        LocalDate endDate = LocalDate.of(2026, 2, 22);
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 2, startDate, endDate);

        //When
        when(isDayOffClient.countPaidDays(startDate, endDate)).thenReturn(2);

        VacationResponse response = vacationPayService.calculateVacationPay(vacationRequest);

        //Then
        assertThat(response.getVacationPay()).isEqualByComparingTo(new BigDecimal("682.59"));
    }

    @Test
    void calculateVacationPay_withOnlyHolidaysInDatesRange_shouldThrowException(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 7);
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 7, startDate, endDate);

        when(isDayOffClient.countPaidDays(startDate, endDate)).thenReturn(0);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Отпуск не может состоять только из праздников");
    }

    @Test
    void calculateVacationPay_withNegativeHolidaysInDatesRange_shouldThrowException(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 7);
        VacationRequest vacationRequest = new VacationRequest(new BigDecimal("10000"), 7, startDate, endDate);

        when(isDayOffClient.countPaidDays(startDate, endDate)).thenReturn(-1);

        //When & Then
        assertThatThrownBy(()->vacationPayService.calculateVacationPay(vacationRequest)).isInstanceOf(IllegalArgumentException.class).hasMessage("Отпуск не может состоять только из праздников");
    }



}
