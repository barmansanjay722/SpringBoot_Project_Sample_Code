package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ContractMasterResponse_BFO {

    @JsonProperty("BFO")
    private List<ContractMasterResponseBody> bfo;

    @JsonProperty("contract_date")
    private String contractDate;
}
