package com.vacation.integration;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


@Component
public class IsDayOffClient {
    private static final Logger log = LoggerFactory.getLogger(IsDayOffClient.class);

    private static final String API_URL = "https://isdayoff.ru/api/getdata";
    private static final String DELIMETER = "%0A";
    private static final int MAX_PERIOD_DAYS = 366;

    private final Map<String, Map<LocalDate, DayInfo>> cache = new ConcurrentHashMap<>();

    private final RestTemplate restTemplate;

    public IsDayOffClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public Map<LocalDate, DayInfo> getDaysInfo(LocalDate startDate, LocalDate endDate) {
        String cacheKey = String.format("%s%s", startDate, endDate);
        return cache.computeIfAbsent(cacheKey, k -> fetchFromApi(startDate, endDate));
    }

    private @NotNull Map<LocalDate, DayInfo> fetchFromApi(LocalDate startDate, LocalDate endDate) {
        validatePeriod(startDate, endDate);

        log.info("Запрос информации о днях с {} по {}", startDate, endDate);
        try {
            String response = callApi(startDate, endDate);

            Map<LocalDate, DayInfo> result = parseResponse(startDate, response);

            log.info("Получена информация о {} днях ", result.size());
            return result;
        } catch (RestClientException e) {
            log.error("Ошибка при вызове API isdayoff.ru", e);
            throw new RuntimeException("Не удалось получить информацию о праздничных днях и выходных");
        }
    }

    public int countPaidDays(LocalDate startDate, LocalDate endDate) {
        return Math.toIntExact(getDaysInfo(startDate, endDate).values().stream().filter(DayInfo::isPaidDay).count());
    }

    private Map<LocalDate, DayInfo> parseResponse(LocalDate startDate, String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new RuntimeException("Пустой ответ от API");
        }
        Map<LocalDate, DayInfo> result = new LinkedHashMap<>();

        String[] lines = response.split(DELIMETER);

        LocalDate date = startDate;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            try {
                int statusCode = Integer.parseInt(line);
                DayStatus status = DayStatus.fromCode(statusCode);
                DayInfo dayInfo = new DayInfo(date, status);

                result.put(date, dayInfo);
                date = date.plusDays(1);
            } catch (NumberFormatException e) {
                log.warn("Некорректный формат строки в ответе API:{}", line);
            }
        }
        return result;
    }

    private String callApi(LocalDate startDate, LocalDate endDate) {
        String formattedStartDate = startDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));
        String formattedEndDate = endDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd"));

        String url = String.format("%s?date1=%s&date2=%s&delimeter=%s", API_URL, formattedStartDate, formattedEndDate, DELIMETER);

        log.debug("Вызов Api:{}", url);

        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(String.format("Api вернул статус %s", response.getStatusCode()));
        }
        return response.getBody();
    }

    private void validatePeriod(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Даты не могут быть null");
        }

        if (!startDate.isBefore(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты конца");
        }

        long daysBetween = ChronoUnit.DAYS.between(startDate, endDate) + 1;
        if (daysBetween > MAX_PERIOD_DAYS) {
            throw new IllegalArgumentException(String.format("Период не может превышать %d дней (запрошено: %d)",
                    MAX_PERIOD_DAYS, daysBetween));
        }
    }
}
