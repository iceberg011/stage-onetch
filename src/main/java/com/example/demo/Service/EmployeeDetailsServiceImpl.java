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
import java.util.Optional;

@Service
public class EmployeeDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private EmployeesRepository employeesRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("=== loadUserByUsername called with: " + username);
        
        // Try to find by email first
        Optional<employees> employeeOpt = employeesRepository.findByEmail(username);
        
        // If not found by email, try by nickname
        if (employeeOpt.isEmpty()) {
            employeeOpt = employeesRepository.findByNickname(username);
            if (employeeOpt.isPresent()) {
                System.out.println("Found by nickname: " + username);
            }
        } else {
            System.out.println("Found by email: " + username);
        }
        
        employees employee = employeeOpt.orElseThrow(() -> 
                new UsernameNotFoundException("Employee not found with username: " + username));

        System.out.println("Employee found: " + employee.getNickname() + " (" + employee.getEmail() + ")");
        
        // Create authorities based on app_role
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        
        if (employee.getApp_role() != null) {
            switch (employee.getApp_role()) {
                case 1:
                    authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("Assigned roles: ROLE_ADMIN, ROLE_USER");
                    break;
                case 2:
                    authorities.add(new SimpleGrantedAuthority("ROLE_MANAGER"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("Assigned roles: ROLE_MANAGER, ROLE_USER");
                    break;
                case 3:
                    authorities.add(new SimpleGrantedAuthority("ROLE_STAFF"));
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("Assigned roles: ROLE_STAFF, ROLE_USER");
                    break;
                default:
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    System.out.println("Assigned roles: ROLE_USER");
                    break;
            }
        } else {
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            System.out.println("Assigned roles: ROLE_USER");
        }

        // Return UserDetails object
        return new User(
                employee.getEmail(),
                employee.getSelf_password() != null ? employee.getSelf_password() : "",
                employee.isIs_active(), // enabled
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}