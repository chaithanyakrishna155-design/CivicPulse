package com.civicpulse.controller;

import com.civicpulse.entity.NotificationPreference;
import com.civicpulse.entity.User;
import com.civicpulse.service.NotificationService;
import com.civicpulse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/notification-preferences")
public class NotificationPreferenceController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    // ==========================
    // Helper Methods
    // ==========================
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    // ==========================
    // Get Preferences
    // ==========================
    @GetMapping
    public ResponseEntity<?> getPreferences(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            NotificationPreference preferences = notificationService.getPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==========================
    // Save Preferences (Create or Update)
    // ==========================
    @PostMapping
    public ResponseEntity<?> savePreferences(@RequestBody NotificationPreference preference, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            preference.setUserId(user.getId().toString());
            notificationService.savePreferences(preference);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to save preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==========================
    // Update Preferences (Alias for POST)
    // ==========================
    @PutMapping
    public ResponseEntity<?> updatePreferences(@RequestBody NotificationPreference preference, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            preference.setUserId(user.getId().toString());
            notificationService.savePreferences(preference);
            return ResponseEntity.ok(preference);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to update preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==========================
    // Get Preferences by User ID (Admin only)
    // ==========================
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getPreferencesByUserId(@PathVariable String userId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        // Check if user is admin
        User currentUser = getLoggedInUser(session);
        if (currentUser == null || !"ADMIN".equals(session.getAttribute("role"))) {
            return ResponseEntity.status(403).body("Access denied. Admin only.");
        }
        
        try {
            NotificationPreference preferences = notificationService.getPreferences(userId);
            return ResponseEntity.ok(preferences);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to get preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==========================
    // Reset Preferences to Default
    // ==========================
    @DeleteMapping("/reset")
    public ResponseEntity<?> resetPreferences(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            NotificationPreference defaultPref = new NotificationPreference();
            defaultPref.setUserId(userId);
            defaultPref.setEmailEnabled(true);
            defaultPref.setSmsEnabled(false);
            defaultPref.setPushEnabled(true);
            defaultPref.setComplaintUpdates(true);
            defaultPref.setAssignmentNotifications(true);
            defaultPref.setResolutionNotifications(true);
            
            notificationService.savePreferences(defaultPref);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Preferences reset to default successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to reset preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    // ==========================
    // Check if preferences exist
    // ==========================
    @GetMapping("/exists")
    public ResponseEntity<?> preferencesExist(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            NotificationPreference preferences = notificationService.getPreferences(userId);
            
            Map<String, Boolean> response = new HashMap<>();
            response.put("exists", preferences != null);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to check preferences: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}