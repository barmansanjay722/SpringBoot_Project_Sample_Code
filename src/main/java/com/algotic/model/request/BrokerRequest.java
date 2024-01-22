package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerRequest {
    @NotBlank(message = "Invalid Name")
    @Pattern(regexp = "[a-zA-Z][a-zA-Z ]+[a-zA-Z]$", message = "Invalid name")
    private String name;

    @NotBlank(message = "Invalid Logo")
    private String logo;
}
