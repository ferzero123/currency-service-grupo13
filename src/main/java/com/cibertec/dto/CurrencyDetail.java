package com.cibertec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CurrencyDetail {
    @JsonProperty("name")
    private String name;

    @JsonProperty("symbol")
    private String symbol;
}