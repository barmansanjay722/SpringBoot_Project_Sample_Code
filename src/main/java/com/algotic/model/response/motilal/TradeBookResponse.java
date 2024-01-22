package com.algotic.model.response.motilal;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = false)
public class TradeBookResponse extends GeneralMotilalResponse {
    private List<TradeBookData> data;
}
