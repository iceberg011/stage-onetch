package com.example.demo.Config;

import com.example.demo.Entity.employees;
import com.example.demo.Service.EmployeeDetailsServiceImpl;
import com.example.demo.Service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.Optional;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Autowired
    private EmployeeDetailsServiceImpl employeeDetailsService;

    @Autowired
    private EmployeeService employeeService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(authz -> authz
                .requestMatchers("/", "/Home", "/accueil", "/about", "/holidays", 
                                "/testimonials", "/services", "/contact", "/signin", 
                                "/signup", "/register", "/api/auth/**").permitAll()
                .requestMatchers("/dashboard/**").hasAnyRole("ADMIN", "MANAGER", "STAFF")
                .requestMatchers("/css/**", "/js/**", "/images/**", "/static/**", 
                               "/webjars/**", "/favicon.ico").permitAll()
                .anyRequest().authenticated()
            )
            .exceptionHandling(exception -> exception
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.sendRedirect("/");
                })
            )
            .formLogin(form -> form
                .loginPage("/signin")
                .loginProcessingUrl("/signin")
                .successHandler(successHandler())
                .failureUrl("/signin?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/signout")
                .logoutSuccessUrl("/signin?logout=true")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400)
                .userDetailsService(employeeDetailsService)
            );

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(employeeDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationSuccessHandler successHandler() {
        return (request, response, authentication) -> {
            String email = authentication.getName();
            Optional<employees> employeeOpt = employeeService.findByEmail(email);

            if (employeeOpt.isPresent()) {
                employees employee = employeeOpt.get();
                HttpSession session = request.getSession();
                session.setAttribute("employee", employee);
                session.setAttribute("employeeId", employee.getId());
                session.setAttribute("nickname", employee.getNickname());
                session.setAttribute("email", employee.getEmail());
                session.setAttribute("firstName", employee.getFirst_name());
                session.setAttribute("lastName", employee.getLast_name());
                session.setAttribute("mobile", employee.getMobile());
                session.setAttribute("empCode", employee.getEmp_code());
                session.setAttribute("appRole", employee.getApp_role());
                session.setAttribute("isActive", employee.isIs_active());

                if (employee.getApp_role() != null && (employee.getApp_role() == 1 || employee.getApp_role() == 2 || employee.getApp_role() == 3)) {
                    response.sendRedirect("/dashboard");
                } else {
                    response.sendRedirect("/");
                }
                return;
            }

            response.sendRedirect("/");
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}