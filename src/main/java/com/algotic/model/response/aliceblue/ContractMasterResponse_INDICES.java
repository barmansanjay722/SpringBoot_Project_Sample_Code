package com.algotic.model.response.aliceblue;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContractMasterResponse_INDICES {

    @JsonProperty("BSE")
    private List<ContractMasterResponseIndicesBody> bse;

    @JsonProperty("NSE")
    private List<ContractMasterResponseIndicesBody> nse;
}
