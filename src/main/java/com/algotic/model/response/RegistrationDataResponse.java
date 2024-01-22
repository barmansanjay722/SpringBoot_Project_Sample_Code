package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrationDataResponse {
    private String name;
    private String email;
    private String phoneNumber;
    private String message;
    private Long days;
}
