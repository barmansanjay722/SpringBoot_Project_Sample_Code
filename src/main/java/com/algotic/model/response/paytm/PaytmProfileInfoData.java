package com.algotic.model.response.paytm;

import java.util.List;
import lombok.Data;

@Data
public class PaytmProfileInfoData {

    private String kycName;

    private String userId;

    private List<String> activeSegments;
}
