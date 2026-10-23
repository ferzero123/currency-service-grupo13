package com.cibertec.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Flags {
    @JsonProperty("png")
    private String png;

    @JsonProperty("svg")
    private String svg;

    @JsonProperty("alt")
    private String alt;
}