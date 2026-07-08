package com.example.demo.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for API endpoints (optional, enable for forms)
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/**")
                .disable()
            )
            
            // Configure URL authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/", "/signin", "/signup").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll() // Only if using H2
                
                // Dashboard requires authentication
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/settings").authenticated()
                
                // Admin endpoints (if you have admin roles)
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure login
            .formLogin(form -> form
                .loginPage("/signin")
                .loginProcessingUrl("/signin")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/signin?error=true")
                .usernameParameter("email")  // Use email as username
                .passwordParameter("password")
                .permitAll()
            )
            
            // Configure logout
            .logout(logout -> logout
                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                .logoutSuccessUrl("/signin?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            
            // Remember me (optional)
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 1 day
                .rememberMeParameter("remember-me")
            )
            
            // Session management
            .sessionManagement(session -> session
                .maximumSessions(1) // Only one session per user
                .expiredUrl("/signin?expired=true")
                .maxSessionsPreventsLogin(false)
            )
            
            // Exception handling
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }
}