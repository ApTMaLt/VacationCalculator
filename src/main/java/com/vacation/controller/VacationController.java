package com.vacation.controller;

import com.vacation.model.request.VacationRequest;
import com.vacation.model.response.VacationResponse;
import com.vacation.service.VacationPayService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api")
public class VacationController {
    private final VacationPayService vacationPayService;

    public VacationController(VacationPayService vacationPayService) {
        this.vacationPayService = vacationPayService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<VacationResponse> calculateVacationPay(
        @RequestParam
        BigDecimal averageSalary,
        @RequestParam
        Integer vacationDays){
        VacationRequest vacationRequest = new VacationRequest(averageSalary, vacationDays);

        VacationResponse vacationResponse = vacationPayService.calculateVacationPay(vacationRequest);
        return ResponseEntity.ok(vacationResponse);
    }




}