package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateCustomerResponse {

    private String userId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private OtpResponse otp;
}
