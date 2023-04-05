package com.mountblue.calendly.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private static final String[] PERMISSIONS_FOR_ALL = {"/invitation/**","/process-date", "/home",
            "/loginPage","/bookingDates","/calendar/*","/conformEvent",
            "/conformBooking","/signup","/upComing","/schedule","/addEvents","/newEvent","/google","/loginPage","/dashboard","/invitation/response/**"
    };

    private static final String[] PERMISSIONS_FOR_USERS = {};

    private UserDetailsService userDetailsService;
    @Autowired
    private UserOAuth2Service userOAuth2Service;
    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    ;

    @Autowired
    public WebSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    WebSecurityConfig() {
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(userDetailsService);
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf()
                .disable()
                .authorizeHttpRequests()
                .requestMatchers(PERMISSIONS_FOR_USERS)
                .hasAnyRole("ADMIN","USER","OWNER")
                .requestMatchers(PERMISSIONS_FOR_ALL)
                .permitAll()
                .anyRequest()
                .authenticated().
                and().
                oauth2Login().
                loginPage("/home").
                userInfoEndpoint().
                userService(userOAuth2Service).
                and()
                .successHandler(oAuth2LoginSuccessHandler)
                .and().logout().logoutUrl("/logout").logoutSuccessUrl("/home");

        return httpSecurity.build();
    }
}
