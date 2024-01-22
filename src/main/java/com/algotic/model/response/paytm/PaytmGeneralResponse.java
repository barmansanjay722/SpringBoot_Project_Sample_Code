package com.algotic.model.response.paytm;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class PaytmGeneralResponse<T> {

    private String status;
    private String message;
    private List<T> data;
    private String errorCode;
}
