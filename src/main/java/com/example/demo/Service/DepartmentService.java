package com.example.demo.Service;

import com.example.demo.Entity.departments;
import java.util.List;

public interface DepartmentService {
    List<departments> findAll();
    departments findById(Long id);
    departments save(departments department);
    void deleteById(Long id);
    String generateDepartmentCode();
    List<departments> searchDepartments(String search, String field, String sort, String name);

    
}