package com.civicpulse.controller;

import com.civicpulse.entity.Notification;
import com.civicpulse.entity.NotificationPreference;
import com.civicpulse.entity.User;
import com.civicpulse.service.NotificationService;
import com.civicpulse.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;
    
    @Autowired
    private UserService userService;

    // ==========================
    // Check Authentication
    // ==========================
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    // ===== VIEW NOTIFICATIONS PAGE =====
    @GetMapping
    public String viewNotifications(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }
        
        String userId = user.getId().toString();
        List<Notification> notifications = notificationService.getUserNotifications(userId);
        long unreadCount = notificationService.getUnreadCount(userId);
        NotificationPreference preferences = notificationService.getPreferences(userId);
        
        model.addAttribute("notifications", notifications);
        model.addAttribute("unreadCount", unreadCount);
        model.addAttribute("preferences", preferences);
        model.addAttribute("userId", userId);
        model.addAttribute("user", user);
        
        return "notifications";
    }

    // ===== API ENDPOINTS =====

    // Get all notifications for user
    @GetMapping("/api/user/{userId}")
    @ResponseBody
    public ResponseEntity<?> getUserNotifications(@PathVariable String userId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        try {
            return ResponseEntity.ok(notificationService.getUserNotifications(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching notifications: " + e.getMessage());
        }
    }

    // Get unread notifications
    @GetMapping("/api/user/{userId}/unread")
    @ResponseBody
    public ResponseEntity<?> getUnreadNotifications(@PathVariable String userId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        try {
            return ResponseEntity.ok(notificationService.getUnreadNotifications(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching unread notifications: " + e.getMessage());
        }
    }

    // Get unread count
    @GetMapping("/api/user/{userId}/count")
    @ResponseBody
    public ResponseEntity<?> getUnreadCount(@PathVariable String userId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        try {
            Map<String, Long> response = new HashMap<>();
            response.put("count", notificationService.getUnreadCount(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching count: " + e.getMessage());
        }
    }

    // Get current user's unread count (convenience endpoint)
    @GetMapping("/api/my-count")
    @ResponseBody
    public ResponseEntity<?> getMyUnreadCount(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            Map<String, Long> response = new HashMap<>();
            response.put("count", notificationService.getUnreadCount(userId));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching count: " + e.getMessage());
        }
    }

    // Mark notification as read
    @PutMapping("/api/mark-read/{notificationId}")
    @ResponseBody
    public ResponseEntity<?> markAsRead(@PathVariable Long notificationId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            notificationService.markAsRead(userId, notificationId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error marking as read: " + e.getMessage());
        }
    }

    // Mark all as read
    @PutMapping("/api/mark-all-read")
    @ResponseBody
    public ResponseEntity<?> markAllAsRead(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            notificationService.markAllAsRead(userId);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications marked as read");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error marking all as read: " + e.getMessage());
        }
    }

    // Get notification preferences
    @GetMapping("/api/preferences")
    @ResponseBody
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
            return ResponseEntity.ok(notificationService.getPreferences(userId));
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error fetching preferences: " + e.getMessage());
        }
    }

    // Save notification preferences
    @PostMapping("/api/preferences")
    @ResponseBody
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
            return ResponseEntity.status(500).body("Error saving preferences: " + e.getMessage());
        }
    }

    // Send test notification
    @PostMapping("/api/test")
    @ResponseBody
    public ResponseEntity<?> sendTestNotification(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            
            Map<String, Object> data = new HashMap<>();
            data.put("title", "Test Notification");
            data.put("message", "This is a test notification from CivicPulse!");
            data.put("complaintId", "123");
            data.put("status", "Testing");
            data.put("updatedAt", LocalDateTime.now().toString());
            data.put("link", "/dashboard");
            
            // Send test email
            notificationService.sendEmailNotification(
                userId,
                user.getEmail(),
                "Test Notification",
                "email/complaint-status",
                data
            );
            
            // Send test SMS (if phone exists)
            if (user.getPhone() != null && !user.getPhone().isEmpty()) {
                notificationService.sendSmsNotification(
                    userId,
                    user.getPhone(),
                    "CivicPulse: This is a test notification!"
                );
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Test notifications sent successfully");
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error sending test notification: " + e.getMessage());
        }
    }

    // ===== WEB PAGE ENDPOINTS =====

    // Delete notification
    @DeleteMapping("/api/delete/{notificationId}")
    @ResponseBody
    public ResponseEntity<?> deleteNotification(@PathVariable Long notificationId, HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        try {
            notificationService.deleteNotification(notificationId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "Notification deleted successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error deleting notification: " + e.getMessage());
        }
    }

    // Clear all notifications
    @DeleteMapping("/api/clear-all")
    @ResponseBody
    public ResponseEntity<?> clearAllNotifications(HttpSession session) {
        if (!isAuthenticated(session)) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        
        User user = getLoggedInUser(session);
        if (user == null) {
            return ResponseEntity.status(401).body("User not found");
        }
        
        try {
            String userId = user.getId().toString();
            notificationService.deleteAllNotifications(userId);
            Map<String, String> response = new HashMap<>();
            response.put("message", "All notifications cleared");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error clearing notifications: " + e.getMessage());
        }
    }
}