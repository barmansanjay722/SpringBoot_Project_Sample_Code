package com.algotic.config;

import com.algotic.base.LogHandlerConfiguration;
import com.algotic.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JwtHelper {
    @Autowired
    private HttpServletRequest httpServletRequest;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private LogHandlerConfiguration logConfig;

    public String getUserId() {
        String token = httpServletRequest.getHeader("Authorization");
        token = token.substring(7);
        String userId = null;
        try {
            userId = jwtTokenUtil.getIdFromToken(token);
        } catch (IllegalArgumentException e) {
            log.warn("Unable to get JWT Token");

        } catch (ExpiredJwtException e) {
            log.warn("JWT Token has expired");
        }
        return userId;
    }
}
