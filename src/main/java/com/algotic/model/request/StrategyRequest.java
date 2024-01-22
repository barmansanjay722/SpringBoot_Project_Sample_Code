package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class StrategyRequest {

    @NotBlank(message = "Please Enter Name it is Mandatory")
    @Pattern(
            regexp = "^[a-zA-Z0-9 ]*$",
            message = "Please enter letters and alphabets in name it doesn't allow special characters")
    private String name;

    @NotBlank(message = "Please Enter Script it is Mandatory")
    private String script;
}
