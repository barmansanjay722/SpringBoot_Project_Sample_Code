package com.algotic.model.response.motilal;

import lombok.Builder;

@Builder
public record ProfileInfoResponse(String status, String message, String errorcode, ProfileData data) {}
