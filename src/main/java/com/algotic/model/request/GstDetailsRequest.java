package com.algotic.model.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@Data
@NoArgsConstructor
public class GstDetailsRequest {
    @NotBlank(message = "Company Name Is mandatory")
    private String companyName;

    @NotBlank(message = "Enter type it is mandatory")
    private String type;

    @NotBlank(message = "Gst number it is Mandatory")
    private String gstIn;

    @NotBlank(message = "Please Enter Address it is mandatory")
    private String address;
}
