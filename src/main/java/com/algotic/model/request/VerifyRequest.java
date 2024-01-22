package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class VerifyRequest {

    @NotBlank(message = "Please Enter Reference Code it is mandatory")
    private String referenceCode;

    @NotBlank(message = "please enter Otp it is mandatory")
    private String otp;
}
