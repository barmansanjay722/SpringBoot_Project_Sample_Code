package com.algotic.model.response;

import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class WatchListRemoveResponse {
    List<WatchListStatusResponse> watchList;
}
