package com.algotic.model.response;

import com.algotic.data.entities.InActiveUsers;
import java.util.List;
import lombok.Data;

@Data
public class InActiveUsersResponse {
    private int total;
    private List<InActiveUsers> result;
}
