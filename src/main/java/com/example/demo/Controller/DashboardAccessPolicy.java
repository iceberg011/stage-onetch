package com.example.demo.Controller;

public final class DashboardAccessPolicy {

    private DashboardAccessPolicy() {
    }

    public static boolean canAccessDashboard(Short appRole) {
        if (appRole == null) {
            return false;
        }

        return appRole == 1 || appRole == 2 || appRole == 3;
    }

    public static String redirectPath(Short appRole) {
        return canAccessDashboard(appRole) ? "/dashboard" : "/";
    }
}
