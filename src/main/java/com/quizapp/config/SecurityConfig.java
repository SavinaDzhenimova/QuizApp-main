package com.quizapp.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.crypto.password.Pbkdf2PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {

        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .requestMatchers(PathRequest.toStaticResources().atCommonLocations()).permitAll()
                        .requestMatchers("/api/**", "/", "/about-us",
                                "/contacts", "/report-problem").permitAll()
                        .requestMatchers("/users/login", "/users/login-error", "/users/register",
                                "/users/forgot-password", "/users/reset-password/**",
                                "/guest/quizzes/**").anonymous()
                        .requestMatchers("/users/logout", "/users/home").authenticated()
                        .requestMatchers("/users/quizzes/**").hasRole("USER")
                        .requestMatchers("/admin/**",
                                "/categories", "/categories/add-category", "/categories/update/**",
                                "/questions", "/questions/add-question", "/questions/update/**",
                                "/statistics/**").hasRole("ADMIN")
                        .requestMatchers("/subscribe",
                                "/contacts/send-inquiry", "/report-problem/send-report",
                                "/start-quiz", "/quizzes/start", "/quizzes/quiz/**",
                                "/subscribe").access((authentication, context) -> {
                                    boolean isAdmin = authentication.get().getAuthorities().stream()
                                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
                                    return new AuthorizationDecision(!isAdmin);
                                })
                        .anyRequest().authenticated()
                )
                .formLogin(formLogin -> formLogin
                        .loginPage("/users/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/users/home", true)
                        .failureUrl("/users/login-error")
                )
                .logout(logout -> logout
                        .logoutUrl("/users/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return Pbkdf2PasswordEncoder.defaultsForSpringSecurity_v5_8();
    }
}