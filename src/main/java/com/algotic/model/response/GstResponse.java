package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GstResponse {
    private String companyName;
    private String type;
    private String gstIn;
    private String userId;
    private String address;
}
