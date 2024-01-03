package com.example.usermanagement;

import static org.springframework.security.config.Customizer.withDefaults;
import static org.springframework.security.web.csrf.CookieCsrfTokenRepository.withHttpOnlyFalse;
import javax.sql.DataSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import lombok.extern.slf4j.Slf4j;

@Configuration
@EnableMethodSecurity
@Slf4j
public class Config {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        var csrfTokenRequestHandler = new CsrfTokenRequestAttributeHandler();

        http
            .logout(logout -> logout
                .logoutUrl("/home/logout"))
            .sessionManagement(session -> session
                .maximumSessions(1))
            .csrf(csrf -> csrf
                .csrfTokenRepository(withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfTokenRequestHandler))
            .authorizeHttpRequests(authorizeHttpRequests -> authorizeHttpRequests
                .requestMatchers("/home/admin/**").hasRole("ADMIN")
                .requestMatchers("/home/sign-up").permitAll()
                .requestMatchers(HttpMethod.GET, "/home/login").permitAll()
                .requestMatchers("/home/forget-password").permitAll()
                .requestMatchers("/home/reset-password").permitAll()
                .anyRequest().authenticated())
            .httpBasic(withDefaults());

        return http.build();
    }

    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        return new JdbcUserDetailsManager(dataSource);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

}
