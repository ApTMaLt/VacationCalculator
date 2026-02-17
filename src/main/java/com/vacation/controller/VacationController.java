package com.vacation.controller;

import com.vacation.model.request.VacationRequest;
import com.vacation.model.response.VacationResponse;
import com.vacation.service.VacationPayService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api")
public class VacationController {
    private final VacationPayService vacationPayService;

    public VacationController(VacationPayService vacationPayService) {
        this.vacationPayService = vacationPayService;
    }

    @GetMapping("/calculate")
    public ResponseEntity<VacationResponse> calculateVacationPay(
            @Valid
            @ModelAttribute
            VacationRequest request) {
        VacationResponse vacationResponse = vacationPayService.calculateVacationPay(request);
        return ResponseEntity.ok(vacationResponse);
    }


}