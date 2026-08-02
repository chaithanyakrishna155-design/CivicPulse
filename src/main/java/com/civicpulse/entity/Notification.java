package com.civicpulse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false)
    private String title;
    
    @Column(nullable = false, length = 1000)
    private String message;
    
    @Column(nullable = false)
    private String type; // EMAIL, SMS, PUSH
    
    @Column(nullable = false)
    private String status; // PENDING, SENT, FAILED
    
    @Column(nullable = false)
    private String recipient;
    
    private String link;
    
    @Column(name = "is_read")
    private boolean read = false;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    // ===== Default Constructor =====
    public Notification() {
    }
    
    // ===== All Args Constructor =====
    public Notification(Long id, String userId, String title, String message, 
                        String type, String status, String recipient, String link, 
                        boolean read, LocalDateTime createdAt, LocalDateTime sentAt) {
        this.id = id;
        this.userId = userId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.status = status;
        this.recipient = recipient;
        this.link = link;
        this.read = read;
        this.createdAt = createdAt;
        this.sentAt = sentAt;
    }
    
    // ===== Lifecycle Callbacks =====
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "PENDING";
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        if (type != null) {
            String normalized = type.toUpperCase();
            if (!normalized.equals("EMAIL") && !normalized.equals("SMS") && !normalized.equals("PUSH")) {
                throw new IllegalArgumentException("Type must be EMAIL, SMS, or PUSH");
            }
            this.type = normalized;
        }
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        if (status != null) {
            String normalized = status.toUpperCase();
            if (!normalized.equals("PENDING") && !normalized.equals("SENT") && !normalized.equals("FAILED")) {
                throw new IllegalArgumentException("Status must be PENDING, SENT, or FAILED");
            }
            this.status = normalized;
            if (normalized.equals("SENT") && sentAt == null) {
                this.sentAt = LocalDateTime.now();
            }
        }
    }
    
    public String getRecipient() {
        return recipient;
    }
    
    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }
    
    public String getLink() {
        return link;
    }
    
    public void setLink(String link) {
        this.link = link;
    }
    
    public boolean isRead() {
        return read;
    }
    
    public void setRead(boolean read) {
        this.read = read;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    // ===== Helper Methods =====
    public boolean isUnread() {
        return !read;
    }
    
    public boolean isSent() {
        return "SENT".equalsIgnoreCase(status);
    }
    
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equalsIgnoreCase(status);
    }
    
    public String getTypeIcon() {
        if ("EMAIL".equalsIgnoreCase(type)) return "📧";
        if ("SMS".equalsIgnoreCase(type)) return "📱";
        if ("PUSH".equalsIgnoreCase(type)) return "🔔";
        return "📨";
    }
    
    public String getTypeBadgeClass() {
        if ("EMAIL".equalsIgnoreCase(type)) return "badge-email";
        if ("SMS".equalsIgnoreCase(type)) return "badge-sms";
        if ("PUSH".equalsIgnoreCase(type)) return "badge-push";
        return "badge-secondary";
    }
    
    public String getStatusBadgeClass() {
        if ("SENT".equalsIgnoreCase(status)) return "badge bg-success";
        if ("PENDING".equalsIgnoreCase(status)) return "badge bg-warning text-dark";
        if ("FAILED".equalsIgnoreCase(status)) return "badge bg-danger";
        return "badge bg-secondary";
    }
    
    public String getTimeAgo() {
        if (createdAt == null) return "Just now";
        
        LocalDateTime now = LocalDateTime.now();
        java.time.Duration duration = java.time.Duration.between(createdAt, now);
        
        long seconds = duration.getSeconds();
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;
        
        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }
    
    public void markAsRead() {
        this.read = true;
    }
    
    public void markAsUnread() {
        this.read = false;
    }
    
    // ===== toString for debugging =====
    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", title='" + title + '\'' +
                ", type='" + type + '\'' +
                ", status='" + status + '\'' +
                ", read=" + read +
                ", createdAt=" + createdAt +
                '}';
    }
}