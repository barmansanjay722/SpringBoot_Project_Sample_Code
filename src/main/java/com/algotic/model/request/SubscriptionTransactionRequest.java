package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionTransactionRequest {
    @NotBlank(message = "Please Enter PaymentId it is mandatory")
    @Pattern(
            regexp = "^([a-zA-Z0-9])+$",
            message = "Please Enter digits and alphabets payment id doesn't contain space and special characters")
    private String paymentId;

    @NotBlank(message = "Please Enter transactionId it is mandatory")
    @Pattern(
            regexp = "^([a-zA-Z0-9])+$",
            message = "Please Enter digits and alphabets transactionId doesn't contain space and special characters")
    private String transactionId;

    @NotBlank(message = "Please Enter status it is mandatory")
    @Size(min = 4, max = 8, message = "Please Enter valid size. It cannot be less than 4 or greater than 8")
    @Pattern(
            regexp = "^([a-zA-Z])+$",
            message =
                    "Please Enter valid status it only contains letters . space numbers and special characters are not allowed in status")
    private String status;

    private Integer subscriptionId;
    private Double amount;
    private Integer brokerId;
}
