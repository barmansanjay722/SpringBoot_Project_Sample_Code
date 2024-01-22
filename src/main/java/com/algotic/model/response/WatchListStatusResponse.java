package com.algotic.model.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WatchListStatusResponse {

    private Integer id;
    private String message;
    private boolean isDeleted;
}
