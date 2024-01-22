package com.algotic.config;

import com.algotic.data.entities.AccessTokens;
import com.algotic.data.entities.Users;
import com.algotic.data.repositories.AccessTokensRepo;
import com.algotic.data.repositories.UsersRepo;
import com.algotic.exception.AlgoticException;
import com.algotic.exception.CommonErrorCode;
import com.algotic.model.response.CustomerResponse;
import com.algotic.services.UserService;
import com.algotic.utils.AlgoticUtils;
import com.algotic.utils.JwtTokenUtil;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    @Autowired
    private AccessTokensRepo accessTokensRepo;

    @Autowired
    private UsersRepo usersRepo;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if (request.getMethod().equals("OPTIONS")) {
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
        } else {
            final String requestTokenHeader = request.getHeader("Authorization");
            String userId = null;
            String jwtToken = null;
            if (requestTokenHeader != null && requestTokenHeader.startsWith("Bearer ")) {
                jwtToken = requestTokenHeader.substring(7);
                try {
                    AccessTokens accessToken = accessTokensRepo.getToken(jwtToken);
                    if (accessToken == null) {
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                        chain.doFilter(request, response);
                        return;
                    }
                    Users users = usersRepo.findByID(accessToken.getUserId());
                    if (users.getRole().equalsIgnoreCase("admin")) {

                        String getAccessToken = accessToken.getToken();
                        if (!getAccessToken.isEmpty()) {
                            userId = jwtTokenUtil.getIdFromToken(jwtToken);
                        }
                    } else if (users.getRole().equalsIgnoreCase("customer")) {

                        if (!AlgoticUtils.isValidAdminUrl(request.getRequestURI())) {
                            String getAccessToken = accessToken.getToken();
                            if (!getAccessToken.isEmpty()) {
                                userId = jwtTokenUtil.getIdFromToken(jwtToken);
                            }
                        } else {
                            throw new AlgoticException(CommonErrorCode.FORBIDDEN_API);
                        }
                    } else {
                        throw new AlgoticException(CommonErrorCode.FORBIDDEN_API);
                    }

                } catch (IllegalArgumentException e) {
                    log.warn("Unable to get JWT Token");
                } catch (ExpiredJwtException e) {
                    log.warn("JWT Token has expired");
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                    chain.doFilter(request, response);
                }
            } else {
                log.warn("JWT Token does not begin with Bearer String");
            }

            // Once we get the token validate it.
            if (userId != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                CustomerResponse customer = this.userService.getCustomerById(userId);

                // if token is valid configure Spring Security to manually set
                // authentication
                if (jwtTokenUtil.validateToken(jwtToken, customer.getId()).equals(true)) {

                    UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(customer, null, null);
                    usernamePasswordAuthenticationToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request));
                    // After setting the Authentication in the context, we specify
                    // that the current user is authenticated. So it passes the
                    // Spring Security Configurations successfully.
                    SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
                }
            }
            chain.doFilter(request, response);
        }
    }
}
