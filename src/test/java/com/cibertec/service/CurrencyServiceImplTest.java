package com.cibertec.service;

import com.cibertec.service.impl.CurrencyServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyServiceImplTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CurrencyServiceImpl currencyService;

    @BeforeEach
    void setUp() {
        org.mockito.MockitoAnnotations.openMocks(this);
    }


    @Test
    void testConvert_success() {

        // Simulación de API externa
        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Double> rates = new HashMap<>();
        rates.put("PEN", 3.80);
        apiResponse.put("rates", rates);

        when(restTemplate.getForObject(
                contains("latest/USD"),
                eq(Map.class))
        ).thenReturn(apiResponse);

        // Ejecución
        Map<String, Object> result = currencyService.convert("USD", "PEN", BigDecimal.valueOf(10));

        // Validación
        assertEquals("USD", result.get("from"));
        assertEquals("PEN", result.get("to"));
        assertEquals(new BigDecimal("10"), result.get("amount"));
        assertEquals(new BigDecimal("38.0"), result.get("result"));
    }


    @Test
    void testConvert_currencyNotFound() {

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Double> rates = new HashMap<>();
        rates.put("EUR", 0.9);
        apiResponse.put("rates", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(apiResponse);

        assertThrows(RuntimeException.class, () -> {
            currencyService.convert("USD", "PEN", BigDecimal.valueOf(10));
        });
    }


    @Test
    void testConvert_apiError() {

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenThrow(new RuntimeException("Error API"));

        assertThrows(RuntimeException.class, () -> {
            currencyService.convert("USD", "PEN", BigDecimal.valueOf(10));
        });
    }


    @Test
    void testGetAvailableCurrencies_success() {

        Map<String, Object> apiResponse = new HashMap<>();
        Map<String, Double> rates = new HashMap<>();
        rates.put("USD", 1.0);
        rates.put("PEN", 3.8);
        apiResponse.put("rates", rates);

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(apiResponse);

        Map<String, String> result = currencyService.getAvailableCurrencies();

        assertTrue(result.containsKey("USD"));
        assertTrue(result.containsKey("PEN"));
    }


    @Test
    void testGetAvailableCurrencies_error() {

        when(restTemplate.getForObject(anyString(), eq(Map.class)))
                .thenReturn(null);

        assertThrows(RuntimeException.class, () -> {
            currencyService.getAvailableCurrencies();
        });
    }
}