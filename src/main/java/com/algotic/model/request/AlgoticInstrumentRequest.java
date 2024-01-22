package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AlgoticInstrumentRequest {
    @NotBlank
    private String token;

    @NotBlank
    private String exchange;
}
