package com.example.demo;

import com.example.demo.Controller.DepartmentsController;
import com.example.demo.Entity.departments;
import com.example.demo.Repository.DepartmentRepository;
import com.example.demo.Repository.LinkRepository;
import com.example.demo.Service.DepartmentService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;

@WebMvcTest(DepartmentsController.class)
class DemoApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DepartmentRepository departmentRepository;

    @MockBean
    private DepartmentService departmentService;

    @MockBean
    private LinkRepository linkRepository;

    @Test
    void departmentEditPageShouldExist() throws Exception {
        mockMvc.perform(get("/dashboard/departments/edit/1"))
                .andExpect(redirectedUrl("/signin"));
    }

}
