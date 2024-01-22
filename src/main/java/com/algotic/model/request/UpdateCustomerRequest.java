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
public class UpdateCustomerRequest {

    @NotBlank(message = "First name  it Is mandatory ")
    @Size(min = 1, max = 50, message = "First name is minimum of 2 alphabets")
    @Pattern(regexp = "^([a-zA-Z])+$", message = " First name Doesn't contain Number, Special character and space ")
    private String firstName;

    @NotBlank(message = "Last Name is mandatory")
    @Size(min = 1, max = 50, message = "Last name is minimum of 2 alphabets")
    @Pattern(regexp = "^([a-zA-Z])+$", message = "Last Name Doesn't contain Number, Special character and space ")
    private String lastName;
}
