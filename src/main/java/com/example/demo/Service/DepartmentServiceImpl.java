package com.example.demo.Service;

import com.example.demo.Entity.departments;
import com.example.demo.Repository.DepartmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public List<departments> findAll() {
        return departmentRepository.findAll();
    }

    @Override
    public departments findById(Long id) {
        Optional<departments> dept = departmentRepository.findById(id);
        return dept.orElse(null);
    }

    @Override
    public departments save(departments department) {
        // Auto-generate dept_code if not provided
        if (department.getdept_code() == null || department.getdept_code().isEmpty()) {
            department.setdept_code(generateDepartmentCode());
        }
        return departmentRepository.save(department);
    }

    @Override
    public void deleteById(Long id) {
        departmentRepository.deleteById(id);
    }

    @Override
    public String generateDepartmentCode() {
        // Get the latest department to determine the next number
        List<departments> allDepts = departmentRepository.findAll();
        
        // If no departments exist, start with DEP001
        if (allDepts.isEmpty()) {
            return "DEP001";
        }
        
        // Find the highest number
        int maxNumber = 0;
        for (departments dept : allDepts) {
            String code = dept.getdept_code();
            if (code != null && code.startsWith("DEP")) {
                try {
                    int number = Integer.parseInt(code.substring(3));
                    if (number > maxNumber) {
                        maxNumber = number;
                    }
                } catch (NumberFormatException e) {
                    // Ignore if format is invalid
                }
            }
        }
        
        // Generate next code
        int nextNumber = maxNumber + 1;
        return String.format("DEP%03d", nextNumber);
    }

    @Override
    public List<departments> searchDepartments(String search, String field, String sort, String name) {
        List<departments> allDepts = departmentRepository.findAll();
        List<departments> result = new ArrayList<>(allDepts);
        
        // Apply search
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase().trim();
            result = result.stream()
                .filter(d -> 
                    (d.getdept_name() != null && d.getdept_name().toLowerCase().contains(searchLower)) ||
                    (d.getdept_code() != null && d.getdept_code().toLowerCase().contains(searchLower))
                )
                .collect(java.util.stream.Collectors.toList());
        }
        
        return result;
    }

    
    
}
