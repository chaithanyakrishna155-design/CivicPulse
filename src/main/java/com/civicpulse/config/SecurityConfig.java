package com.civicpulse.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {


        http

            // Disable CSRF for Thymeleaf forms
            .csrf(csrf -> csrf.disable())


            // Allow everything because you handle login using HttpSession
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/",
                        "/login",
                        "/register",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/uploads/**",
                        "/favicon.ico",
                        "/error"
                )
                .permitAll()


                .anyRequest()
                .permitAll()
            )


            // IMPORTANT
            // Disable Spring Security login
            .formLogin(form -> form.disable())


            // Disable Spring Security logout
            .logout(logout -> logout.disable());


        return http.build();
    }
}