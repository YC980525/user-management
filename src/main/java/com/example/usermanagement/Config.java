package com.example.usermanagement;

import static org.springframework.security.config.Customizer.withDefaults;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class Config {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{

        http
            .formLogin(withDefaults())
            .csrf(withDefaults())
            .authorizeHttpRequests(request ->
                request.requestMatchers("/home/**").authenticated()
            )
            .httpBasic(withDefaults());

        var securityFilterChain = http.build();

        return securityFilterChain;
    }

    // @Bean
    // public UserDetailsService testOnlyUsers(PasswordEncoder passwordEncoder) {
    //     User.UserBuilder users = User.builder();
    //     UserDetails admin = users
    //         .username("admin")
    //         .password(passwordEncoder.encode("password"))
    //         .roles("USER", "ADMIN")
    //         .build();
    //     return new InMemoryUserDetailsManager(admin);
    // }

    @Bean
    public UserDetailsManager users(DataSource dataSource) {
        UserDetails admin = User.builder()
            .username("admin")
            .password("{noop}password")
            .roles("USER", "ADMIN")
            .build();
        JdbcUserDetailsManager users = new JdbcUserDetailsManager(dataSource);
        users.createUser(admin);
        return users;
    }

    // @Bean
    // public PasswordEncoder passwordEncoder() {
    //     return new BCryptPasswordEncoder();
    // }
}
