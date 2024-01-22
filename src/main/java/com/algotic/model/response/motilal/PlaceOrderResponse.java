package com.algotic.model.response.motilal;

/**
 * This record responsible to get motilal specific response
 *
 */
public record PlaceOrderResponse(String status, String message, String errorcode, String uniqueorderid) {}
