package com.cibertec.controller;

import com.cibertec.dto.ConversionRequest;
import com.cibertec.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/currency")
@RequiredArgsConstructor
public class CurrencyController {

    private final CurrencyService currencyService;

    @PostMapping("/convert")
    public ResponseEntity<?> convertCurrency(@RequestBody ConversionRequest request) {

        return ResponseEntity.ok(
                currencyService.convert(
                        request.from(),
                        request.to(),
                        request.amount()
                )
        );
    }

    @GetMapping("/exchange-rate/{name}")
    public ResponseEntity<?> getExchangeRate(@PathVariable String name) {
        return ResponseEntity.ok(currencyService.getCurrenciesByCountryName(name));
    }

    @GetMapping("/currencies")
    public ResponseEntity<?> getCurrencies() {
        return ResponseEntity.ok(currencyService.getAvailableCurrencies());
    }


}

