package com.cibertec.service;

import com.cibertec.dto.Currency;
import java.math.BigDecimal;
import java.util.Map;

public interface CurrencyService {
    Map<String, Currency> getCurrenciesByCountryName(String name);
    Map<String, Object> convert(String from, String to, BigDecimal amount);
    Map<String, String> getAvailableCurrencies();
}