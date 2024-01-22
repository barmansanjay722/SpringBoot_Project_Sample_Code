package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResendOtpRequest {
    @NotBlank(message = "Invalid Reference code")
    @Size(min = 0, max = 7)
    @Pattern(regexp = "^[a-z_-]*$", message = "Invalid Reference Code")
    private String referenceCode;
}
