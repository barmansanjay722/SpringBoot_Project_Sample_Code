package com.algotic.utils;

import com.algotic.constants.motilal.MotilalHeader;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class MotilalUtils {

    /**
     * @param token
     * @param apiKey
     * @return
     */
    public static HttpHeaders getMotilalHeader(String token, String apiKey) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
        headers.add(HttpHeaders.AUTHORIZATION, token);
        headers.add(HttpHeaders.USER_AGENT, "MOSL/V.1.1.0");
        headers.add(MotilalHeader.SOURCE_ID, "WEB");
        headers.add(MotilalHeader.API_KEY, apiKey);

        headers.add(MotilalHeader.MAC_ADDRESS, AlgoticUtils.getMacAddress());
        headers.add(MotilalHeader.LOCAL_IP, AlgoticUtils.getLocalIp());
        headers.add(MotilalHeader.PUBLIC_IP, AlgoticUtils.getPublicIp());
        return headers;
    }
}
