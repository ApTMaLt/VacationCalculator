package com.example.VacationCalculator.integration;

import com.vacation.exception.InvalidVacationRequestException;
import com.vacation.integration.DayInfo;
import com.vacation.integration.DayStatus;
import com.vacation.integration.IsDayOffClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IsDayOffClientTest {

    @Mock
    RestTemplate restTemplate;

    IsDayOffClient isDayOffClient;

    @BeforeEach
    void setUp() {
        isDayOffClient = new IsDayOffClient(restTemplate);
    }

    @Test
    void getDaysInfo_withWorkingDays_shouldParseCorrectly() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 19);
        LocalDate endDate = LocalDate.of(2026, 2, 20);
        String apiResponse = "0%0A0";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260219&date2=20260220&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        Map<LocalDate, DayInfo> result = isDayOffClient.getDaysInfo(startDate, endDate);


        //Then
        assertThat(result).hasSize(2);
        // четверг рабочий день
        assertThat(result.get(startDate).getStatus()).isEqualTo(DayStatus.WORKING_DAY);
        assertThat(result.get(startDate).isPaidDay()).isTrue();
        assertThat(result.get(startDate).isUnpaidDay()).isFalse();
        // пятница рабочий день
        assertThat(result.get(startDate).getStatus()).isEqualTo(DayStatus.WORKING_DAY);
        assertThat(result.get(startDate).isPaidDay()).isTrue();
        assertThat(result.get(startDate).isUnpaidDay()).isFalse();
    }

    @Test
    void getDaysInfo_witRangeWorkingAndWeekendAndHolidayDays_shouldParseCorrectly() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 20);
        LocalDate endDate = LocalDate.of(2026, 2, 23);
        String apiResponse = "0%0A1%0A1%0A8";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260220&date2=20260223&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        Map<LocalDate, DayInfo> result = isDayOffClient.getDaysInfo(startDate, endDate);


        //Then
        assertThat(result).hasSize(4);

        // пятница рабочий день
        assertThat(result.get(LocalDate.of(2026, 2, 20)).getStatus()).isEqualTo(DayStatus.WORKING_DAY);
        assertThat(result.get(LocalDate.of(2026, 2, 20)).isPaidDay()).isTrue();
        assertThat(result.get(LocalDate.of(2026, 2, 20)).isUnpaidDay()).isFalse();

        // суббота выходной
        assertThat(result.get(LocalDate.of(2026, 2, 21)).getStatus()).isEqualTo(DayStatus.NON_WORKING_DAY);
        assertThat(result.get(LocalDate.of(2026, 2, 21)).isPaidDay()).isTrue();
        assertThat(result.get(LocalDate.of(2026, 2, 21)).isUnpaidDay()).isFalse();

        // воскресенье выходной
        assertThat(result.get(LocalDate.of(2026, 2, 22)).getStatus()).isEqualTo(DayStatus.NON_WORKING_DAY);
        assertThat(result.get(LocalDate.of(2026, 2, 22)).isPaidDay()).isTrue();
        assertThat(result.get(LocalDate.of(2026, 2, 22)).isUnpaidDay()).isFalse();

        // понедельник праздник День защитника отечества
        assertThat(result.get(LocalDate.of(2026, 2, 23)).getStatus()).isEqualTo(DayStatus.HOLIDAY);
        assertThat(result.get(LocalDate.of(2026, 2, 23)).isPaidDay()).isFalse();
        assertThat(result.get(LocalDate.of(2026, 2, 23)).isUnpaidDay()).isTrue();
    }

    @Test
    void getDaysInfo_withEmptyResponse_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessage("Пустой ответ от API");
    }

    @Test
    void getDaysInfo_withApiError100_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "100";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessage("Ошибка API isdayoff.ru: 100");
    }

    @Test
    void getDaysInfo_withApiError101_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "101";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessage("Ошибка API isdayoff.ru: 101");
    }

    @Test
    void getDaysInfo_withApiError199_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "199";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessage("Ошибка API isdayoff.ru: 199");
    }

    @Test
    void getDaysInfo_withInvalidStatusCode_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "102";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(InvalidVacationRequestException.class).hasMessage("Неизвестный код статуса дня");
    }

    @Test
    void getDaysInfo_withRestClientException_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);
        String apiResponse = "102";

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenThrow(new RestClientException("Network error"));

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessage("Не удалось получить информацию о праздничных днях и выходных");

    }

    @Test
    void getDaysInfo_withHttpError_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);


        ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.getForEntity(any(String.class), eq(String.class))).thenReturn(responseEntity);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, localDate.plusDays(1))).isInstanceOf(RuntimeException.class).hasMessageStartingWith("Api вернул статус");

    }

    @Test
    void getDaysInfo_withNullStartDate_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(null, localDate.plusDays(1))).isInstanceOf(IllegalArgumentException.class).hasMessage("Даты не могут быть null");

    }

    @Test
    void getDaysInfo_withNullEndDate_shouldThrowException() {
        // Given
        LocalDate localDate = LocalDate.of(2026, 2, 19);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(localDate, null)).isInstanceOf(RuntimeException.class).hasMessage("Даты не могут быть null");

    }

    @Test
    void getDaysInfo_withEndBeforeStartDate_shouldThrowException() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 19);
        LocalDate endDate = LocalDate.of(2026, 2, 18);

        //When & Then
        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(startDate, endDate)).isInstanceOf(IllegalArgumentException.class).hasMessage("Дата начала не может быть позже даты конца");

    }

    @Test
    void getDaysInfo_withPeriodExceedingMaxPeriodDays_shouldThrowException() {
        LocalDate startDate = LocalDate.of(2026, 2, 19);

        int maxPeriodDays = IsDayOffClient.getMaxPeriodDays();

        LocalDate endDate = startDate.plusDays(maxPeriodDays);


        assertThatThrownBy(() -> isDayOffClient.getDaysInfo(startDate, endDate)).isInstanceOf(IllegalArgumentException.class).hasMessage(String.format("Период не может превышать %d дней (запрошено: %d)",
                maxPeriodDays, maxPeriodDays + 1));

    }

    @Test
    void countPaidDays_withWorkingDays_shouldReturnCorrectCount() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 19); //четверг рабочий день
        LocalDate endDate = LocalDate.of(2026, 2, 20); //пятница рабочий день
        String apiResponse = "0%0A0";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260219&date2=20260220&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        int result = isDayOffClient.countPaidDays(startDate, endDate);


        //Then
        assertThat(result).isEqualTo(2);

    }

    @Test
    void countPaidDays_witMixedDays_shouldReturnCorrectCount() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 2, 20);
        LocalDate endDate = LocalDate.of(2026, 2, 23);
        // пятница(рабочий день) суббота(выходной) воскресенье(выходной) понедельник(праздник)
        String apiResponse = "0%0A1%0A1%0A8";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260220&date2=20260223&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        int result = isDayOffClient.countPaidDays(startDate, endDate);


        //Then
        assertThat(result).isEqualTo(3);

    }

    @Test
    void countPaidDays_witOnlyHolidays_shouldReturnCorrectCount() {
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 3);
        // праздники новогодние
        String apiResponse = "8%0A8%0A8";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260103&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        int result = isDayOffClient.countPaidDays(startDate, endDate);

        //Then
        assertThat(result).isEqualTo(0);

    }

    @Test
    void getDaysInfo_withSamePeriodCalledTwice_shouldUseCache(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 3);
        String apiResponse = "8%0A8%0A8";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260103&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        Map<LocalDate, DayInfo> result1 = isDayOffClient.getDaysInfo(startDate, endDate);
        Map<LocalDate, DayInfo>  result2 = isDayOffClient.getDaysInfo(startDate, endDate);

        //Then
        assertThat(result1).isEqualTo(result2);

        verify(restTemplate).getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260103&delimeter=%0A"), eq(String.class));

    }

    @Test
    void getDaysInfo_withSamePeriodCalledTwice_shouldBeEqual(){
        // Given
        LocalDate startDate = LocalDate.of(2026, 1, 1);
        LocalDate endDate = LocalDate.of(2026, 1, 3);
        String apiResponse = "8%0A8%0A8";

        ResponseEntity<String> responseEntity = new ResponseEntity<>(apiResponse, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260103&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity);

        //When
        Map<LocalDate, DayInfo> result1 = isDayOffClient.getDaysInfo(startDate, endDate);
        Map<LocalDate, DayInfo>  result2 = isDayOffClient.getDaysInfo(startDate, endDate);

        //Then
        assertThat(result1).isEqualTo(result2);

    }

    @Test
    void getDaysInfo_withDifferentPeriods_shouldMakeSeparateCalls(){
        // Given
        LocalDate startDate1 = LocalDate.of(2026, 1, 1);
        LocalDate endDate1 = LocalDate.of(2026, 1, 2);
        String apiResponse1 = "8%0A8";

        LocalDate startDate2 = LocalDate.of(2026, 2, 1);
        LocalDate endDate2 = LocalDate.of(2026, 2, 2);
        String apiResponse2 = "1%0A0";

        ResponseEntity<String> responseEntity1 = new ResponseEntity<>(apiResponse1, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260102&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity1);

        ResponseEntity<String> responseEntity2 = new ResponseEntity<>(apiResponse2, HttpStatus.OK);

        when(restTemplate.getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260201&date2=20260202&delimeter=%0A"), eq(String.class))).thenReturn(responseEntity2);

        //When
        Map<LocalDate, DayInfo> result1 = isDayOffClient.getDaysInfo(startDate1, endDate1);
        Map<LocalDate, DayInfo>  result2 = isDayOffClient.getDaysInfo(startDate2, endDate2);

        //Then
        assertThat(result1.get(startDate1).getStatus()).isEqualTo(DayStatus.HOLIDAY);
        assertThat(result2.get(startDate2).getStatus()).isEqualTo(DayStatus.NON_WORKING_DAY);

        verify(restTemplate).getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260101&date2=20260102&delimeter=%0A"), eq(String.class));
        verify(restTemplate).getForEntity(eq("https://isdayoff.ru/api/getdata?date1=20260201&date2=20260202&delimeter=%0A"), eq(String.class));

    }


}
