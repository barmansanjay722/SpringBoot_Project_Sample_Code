package com.algotic.model.response;

import com.algotic.data.entities.SubscriptionRenewalDatas;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SubscriptionRenewalResponse {
    private Integer total;
    private List<SubscriptionRenewalDatas> result;
}
