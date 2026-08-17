package com.example.demo.Service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class LocationService {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    // Get client IP address from request
    public String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // If multiple IPs, take the first one
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    // Get location from IP address using ipapi.co
    public LocationInfo getLocationFromIp(String ip) {
        try {
            // Use ipapi.co for geolocation (free, no API key required)
            String url = "https://ipapi.co/" + ip + "/json/";
            String response = restTemplate.getForObject(url, String.class);
            
            JsonNode json = objectMapper.readTree(response);
            
            LocationInfo location = new LocationInfo();
            location.setIp(ip);
            
            if (json.has("city") && !json.get("city").isNull()) {
                location.setCity(json.get("city").asText());
            }
            if (json.has("region") && !json.get("region").isNull()) {
                location.setRegion(json.get("region").asText());
            }
            if (json.has("country_name") && !json.get("country_name").isNull()) {
                location.setCountry(json.get("country_name").asText());
            }
            if (json.has("latitude") && !json.get("latitude").isNull()) {
                location.setLatitude(json.get("latitude").asDouble());
            }
            if (json.has("longitude") && !json.get("longitude").isNull()) {
                location.setLongitude(json.get("longitude").asDouble());
            }
            if (json.has("timezone") && !json.get("timezone").isNull()) {
                location.setTimezone(json.get("timezone").asText());
            }
            
            return location;
        } catch (Exception e) {
            System.err.println("Error getting location from IP: " + e.getMessage());
            // Return default location if geolocation fails
            LocationInfo location = new LocationInfo();
            location.setIp(ip);
            location.setCity("Unknown");
            location.setCountry("Unknown");
            return location;
        }
    }

    // Inner class for location info
    public static class LocationInfo {
        private String ip;
        private String city;
        private String region;
        private String country;
        private double latitude;
        private double longitude;
        private String timezone;

        // Getters and Setters
        public String getIp() { return ip; }
        public void setIp(String ip) { this.ip = ip; }
        
        public String getCity() { return city; }
        public void setCity(String city) { this.city = city; }
        
        public String getRegion() { return region; }
        public void setRegion(String region) { this.region = region; }
        
        public String getCountry() { return country; }
        public void setCountry(String country) { this.country = country; }
        
        public double getLatitude() { return latitude; }
        public void setLatitude(double latitude) { this.latitude = latitude; }
        
        public double getLongitude() { return longitude; }
        public void setLongitude(double longitude) { this.longitude = longitude; }
        
        public String getTimezone() { return timezone; }
        public void setTimezone(String timezone) { this.timezone = timezone; }
        
        public String getFullLocation() {
            StringBuilder sb = new StringBuilder();
            if (city != null && !city.isEmpty()) sb.append(city);
            if (region != null && !region.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(region);
            }
            if (country != null && !country.isEmpty()) {
                if (sb.length() > 0) sb.append(", ");
                sb.append(country);
            }
            return sb.length() > 0 ? sb.toString() : "Unknown Location";
        }
    }
}