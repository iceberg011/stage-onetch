package com.example.demo.Service;

import com.example.demo.Entity.employees;
import com.example.demo.Repository.EmployeesRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeUserDetailsService implements UserDetailsService {

    @Autowired
    private EmployeesRepository employeesRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        employees employee = employeesRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Create authorities/roles
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        // Add role based on app_role
        if (employee.getApp_role() != null) {
            switch (employee.getApp_role()) {
                case 1:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    break;
                case 2:
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    break;
                case 3:
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                    break;
                default:
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    break;
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }

        return new User(
                employee.getEmail(),
                employee.getSelf_password() != null ? employee.getSelf_password() : "",
                employee.isIs_active(),
                true,
                true,
                true,
                authorities
        );
    }
}