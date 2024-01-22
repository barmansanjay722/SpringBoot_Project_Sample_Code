package com.algotic.model.request;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class CreateCustomerRequest {

    @NotBlank(message = "First name Is mandatory ")
    @Size(min = 1, max = 50, message = "First name is minimum of 2 alphabets")
    @Pattern(regexp = "^([a-zA-Z])+$", message = " First name Doesn't contain Number, Special character and space ")
    private String firstName;

    @NotBlank(message = "Last Name is mandatory")
    @Size(min = 1, max = 50, message = "Last name is minimum of 2 alphabets")
    @Pattern(regexp = "^([a-zA-Z])+$", message = "Last Name Doesn't contain Number, Special character and space ")
    private String lastName;

    @NotBlank(message = "Please Enter Email it is mandatory")
    @Pattern(
            regexp = "^(\\w[a-zA-Z0-9.]+@[a-zA-Z0-9.]+\\.[a-zA-Z]{2,5})$",
            message = "Please Enter Valid  Email Accept @ and dot other Special characters and space are not allowed")
    private String email;

    @NotBlank(message = "Phone Number is mandatory")
    @Size(min = 10, max = 10, message = "Phone number contains maximum 10 digit")
    @Pattern(
            regexp = "^(?:(?:\\+|0{0,2})91(\\s*[\\-]\\s*)?|[0]?)?[6789]\\d{9}$",
            message = "Phone number only contains digits")
    private String phoneNumber;
}
