package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BrokerResponse {

    private Integer id;
    private String name;
    private String logo;
    private String authUrl;
    private String onboardingUrl;
}
