package com.example.demo;

import com.example.demo.Controller.DashboardAccessPolicy;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AccessPolicyTest {

    @Test
    void regularUserCannotAccessDashboard() {
        assertFalse(DashboardAccessPolicy.canAccessDashboard((short) 4));
        assertEquals("/", DashboardAccessPolicy.redirectPath((short) 4));
    }

    @Test
    void staffAndAdminCanAccessDashboard() {
        assertTrue(DashboardAccessPolicy.canAccessDashboard((short) 1));
        assertTrue(DashboardAccessPolicy.canAccessDashboard((short) 2));
        assertTrue(DashboardAccessPolicy.canAccessDashboard((short) 3));
        assertEquals("/dashboard", DashboardAccessPolicy.redirectPath((short) 3));
    }
}
