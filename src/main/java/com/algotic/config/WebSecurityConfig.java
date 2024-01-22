package com.algotic.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebSecurityConfig {

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private JwtRequestFilter jwtRequestFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors()
                .and()
                .csrf()
                .disable()
                .addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class)
                // don't authenticate this particular request
                .authorizeHttpRequests()
                .requestMatchers(
                        "/api/customer",
                        "/api/login/{type}",
                        "/api/otp/verify",
                        "/api/syncInstruments",
                        "/api/aliceblue/webhook",
                        "/api/tradingview/webhooks/{webhookUrlData}",
                        "/api/customer/email/verify/{verificationCode}",
                        "/api/webhook/instamojo/payment",
                        "/api/contactUs",
                        "/api/otp/resend",
                        "/api/termsAccepted")
                .permitAll()
                .
                // all other requests need to be authenticated
                anyRequest()
                .authenticated()
                .and()
                .
                // make sure we use stateless session; session won't be used to
                // store user's state.
                exceptionHandling()
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        return http.build();
    }
}
