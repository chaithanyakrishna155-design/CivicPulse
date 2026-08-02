package com.civicpulse.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "notification_preferences")
public class NotificationPreference {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;
    
    @Column(name = "email_enabled")
    private boolean emailEnabled = true;
    
    @Column(name = "sms_enabled")
    private boolean smsEnabled = false;
    
    @Column(name = "push_enabled")
    private boolean pushEnabled = true;
    
    @Column(name = "complaint_updates")
    private boolean complaintUpdates = true;
    
    @Column(name = "assignment_notifications")
    private boolean assignmentNotifications = true;
    
    @Column(name = "resolution_notifications")
    private boolean resolutionNotifications = true;
    
    // ===== Default Constructor =====
    public NotificationPreference() {
    }
    
    // ===== Getters and Setters =====
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public boolean isEmailEnabled() {
        return emailEnabled;
    }
    
    public void setEmailEnabled(boolean emailEnabled) {
        this.emailEnabled = emailEnabled;
    }
    
    public boolean isSmsEnabled() {
        return smsEnabled;
    }
    
    public void setSmsEnabled(boolean smsEnabled) {
        this.smsEnabled = smsEnabled;
    }
    
    public boolean isPushEnabled() {
        return pushEnabled;
    }
    
    public void setPushEnabled(boolean pushEnabled) {
        this.pushEnabled = pushEnabled;
    }
    
    public boolean isComplaintUpdates() {
        return complaintUpdates;
    }
    
    public void setComplaintUpdates(boolean complaintUpdates) {
        this.complaintUpdates = complaintUpdates;
    }
    
    public boolean isAssignmentNotifications() {
        return assignmentNotifications;
    }
    
    public void setAssignmentNotifications(boolean assignmentNotifications) {
        this.assignmentNotifications = assignmentNotifications;
    }
    
    public boolean isResolutionNotifications() {
        return resolutionNotifications;
    }
    
    public void setResolutionNotifications(boolean resolutionNotifications) {
        this.resolutionNotifications = resolutionNotifications;
    }
    
    // ===== Helper Methods =====
    public boolean hasAnyChannelEnabled() {
        return emailEnabled || smsEnabled || pushEnabled;
    }
    
    public boolean isAllChannelsDisabled() {
        return !emailEnabled && !smsEnabled && !pushEnabled;
    }
    
    public void enableAllChannels() {
        this.emailEnabled = true;
        this.smsEnabled = true;
        this.pushEnabled = true;
    }
    
    public void disableAllChannels() {
        this.emailEnabled = false;
        this.smsEnabled = false;
        this.pushEnabled = false;
    }
    
    @Override
    public String toString() {
        return "NotificationPreference{" +
                "userId='" + userId + '\'' +
                ", emailEnabled=" + emailEnabled +
                ", smsEnabled=" + smsEnabled +
                ", pushEnabled=" + pushEnabled +
                '}';
    }
}