package com.example.demo.Config;

import com.example.demo.Entity.UserAccount;
import com.example.demo.Service.UserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import java.io.IOException;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private UserService userService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for testing
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/auth/**")
                .disable()
            )
            
            // Configure URL authorization
            .authorizeHttpRequests(auth -> auth
                // Public endpoints - no authentication required
                .requestMatchers("/", "/signin", "/signup", "/create-test-user").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**", "/webjars/**").permitAll()
                .requestMatchers("/h2-console/**").permitAll()
                
                // Dashboard requires authentication
                .requestMatchers("/dashboard").authenticated()
                .requestMatchers("/profile").authenticated()
                .requestMatchers("/settings").authenticated()
                
                // Admin endpoints
                .requestMatchers("/admin/**").hasRole("ADMIN")
                
                // All other requests require authentication
                .anyRequest().authenticated()
            )
            
            // Configure login with custom success handler
            .formLogin(form -> form
                .loginPage("/signin")
                .loginProcessingUrl("/signin")
                .successHandler(customAuthenticationSuccessHandler())  // Custom success handler
                .failureUrl("/signin?error=true")
                .usernameParameter("email")
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
            
            // Remember me
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
                .rememberMeParameter("remember-me")
            )
            
            // Session management
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/signin?expired=true")
                .maxSessionsPreventsLogin(false)
            )
            
            // Exception handling
            .exceptionHandling(exception -> exception
                .accessDeniedPage("/access-denied")
            );

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler customAuthenticationSuccessHandler() {
        return new AuthenticationSuccessHandler() {
            @Override
            public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, 
                                                Authentication authentication) throws IOException, ServletException {
                System.out.println("=== Authentication Success Handler called ===");
                
                // Get the authenticated user's email
                String email = authentication.getName();
                System.out.println("Authenticated email: " + email);
                
                // Get the user from database
                UserAccount user = userService.getUserByEmail(email);
                System.out.println("User found: " + (user != null ? user.getUsername() : "null"));
                
                if (user != null) {
                    // Update login information
                    userService.updateUserLoginInfo(user);
                    
                    // Set session attributes
                    request.getSession().setAttribute("user", user);
                    request.getSession().setAttribute("username", user.getUsername());
                    request.getSession().setAttribute("email", user.getEmail());
                    request.getSession().setAttribute("userId", user.getId());
                    request.getSession().setAttribute("firstName", user.getFirst_name());
                    request.getSession().setAttribute("lastName", user.getLast_name());
                    
                    System.out.println("Session attributes set for user: " + user.getUsername());
                }
                
                // Redirect to dashboard
                response.sendRedirect("/dashboard");
            }
        };
    }
}