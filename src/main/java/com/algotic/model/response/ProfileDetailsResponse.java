package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ProfileDetailsResponse {
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private String email;
    private String isActiveSubscription;
    private String freeBrokerAccountStatus;
}
