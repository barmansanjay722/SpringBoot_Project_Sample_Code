package com.algotic.model.request.aliceblue;

import com.algotic.model.request.IBrokerSessionRequest;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class AliceBlueSessionRequest implements IBrokerSessionRequest {
    @NotBlank(message = "Enter User Id it is mandatory")
    @Pattern(regexp = "^[0-9]*$", message = "Enter only number no space and special character are allowed in User Id")
    private String userID;

    @Min(value = 0, message = "Enter Broker Id It is mandatory or greater than 0")
    private Integer brokerID;

    @NotBlank(message = "Enter Auth Code  it is Mandatory ")
    @Pattern(
            regexp = "^[a-zA-Z0-9]*$",
            message = "Enter only number and character no space and special character are allowed")
    private String authCode;

    @NotBlank(message = "Enter App Code  it is Mandatory")
    @Pattern(regexp = "^[a-zA-Z]*$", message = "Enter Valid AppCode it only contain alphabets ")
    private String appCode;
}
