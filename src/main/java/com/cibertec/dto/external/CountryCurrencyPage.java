package com.cibertec.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public record CountryCurrencyPage(Boolean success, PageData data) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record PageData(List<CountryData> objects, @JsonProperty("_demo") Map<String, Object> demo) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CountryData(List<CurrencyItem> currencies, FlagData flag) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record CurrencyItem(String code, String name, String symbol) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record FlagData(@JsonProperty("url_png") String png,
                           @JsonProperty("url_svg") String svg,
                           @JsonProperty("description") String alt) {}
}
