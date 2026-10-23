package com.cibertec.service.impl;

import com.cibertec.dto.Currency;
import com.cibertec.client.CountryCurrencyClient;
import com.cibertec.service.CurrencyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CurrencyServiceImpl implements CurrencyService {

    private final RestTemplate restTemplate;
    private final CountryCurrencyClient countryCurrencyClient;

    @Override
    public Map<String, Currency> getCurrenciesByCountryName(String name) {
        return Map.of("currency", countryCurrencyClient.getByCountryName(name));
    }

    @Override
    public Map<String, Object> convert(String from, String to, BigDecimal amount) {

        try {

            String url = "https://open.er-api.com/v6/latest/" + from;

            Map<String, Object> apiResponse = restTemplate.getForObject(url, Map.class);

            if (apiResponse == null || !apiResponse.containsKey("rates")) {
                throw new RuntimeException("Error obteniendo tasas");
            }

            Map<String, Double> rates = (Map<String, Double>) apiResponse.get("rates");

            Double rate = rates.get(to);

            if (rate == null) {
                throw new RuntimeException("Moneda destino no encontrada");
            }

            BigDecimal result = amount.multiply(BigDecimal.valueOf(rate));

            return Map.of(
                    "from", from,
                    "to", to,
                    "amount", amount,
                    "result", result
            );

        } catch (Exception e) {
            throw new RuntimeException("Error llamando API externa", e);
        }

    }
    @Override
    public Map<String, String> getAvailableCurrencies() {

        String url = "https://open.er-api.com/v6/latest/USD";

        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response == null || !response.containsKey("rates")) {
            throw new RuntimeException("Error obteniendo monedas");
        }

        Map<String, Double> rates = (Map<String, Double>) response.get("rates");

        Map<String, String> currencies = new HashMap<>();

        for (String key : rates.keySet()) {
            currencies.put(key, key);
        }

        return currencies;
    }
}