package com.algotic.model.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaperPriceAndQuantity {

    private Double buyAvrge;
    private Double sellAvrge;
    private Integer buyQty;
    private Integer sellQty;
}
