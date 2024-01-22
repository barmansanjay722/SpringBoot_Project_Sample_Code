package com.algotic.model.response.motilal;

import java.util.List;

public record ProfileData(
        String clientcode,
        String name,
        String usertype,
        List<String> exchanges,
        List<String> products,
        String poastatus) {}
