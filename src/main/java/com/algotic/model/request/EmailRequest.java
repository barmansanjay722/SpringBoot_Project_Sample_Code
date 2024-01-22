package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EmailRequest {
    @NotBlank(message = "Please Enter Email it is mandatory")
    @Pattern(
            regexp = "^(\\w[a-zA-Z0-9.]+@[a-zA-Z0-9.]+\\.[a-zA-Z]{2,5})$",
            message = "Please Enter Valid  Email Accept @ and dot other Special characters and space are not allowed")
    private String email;
}
