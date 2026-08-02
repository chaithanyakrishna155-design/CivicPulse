package com.civicpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "push_subscriptions", 
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "endpoint"})
       })
public class PushSubscription {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Column(nullable = false, length = 1000)
    private String endpoint;
    
    @Column(name = "p256dh_key", nullable = false, length = 500)
    private String p256dhKey;
    
    @Column(name = "auth_key", nullable = false, length = 500)
    private String authKey;
    
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
    
    @Column(name = "is_active")
    private boolean active = true;
    
    // ===== Constructors =====
    public PushSubscription() {
    }
    
    // ===== Getters and Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public String getP256dhKey() { return p256dhKey; }
    public void setP256dhKey(String p256dhKey) { this.p256dhKey = p256dhKey; }
    
    public String getAuthKey() { return authKey; }
    public void setAuthKey(String authKey) { this.authKey = authKey; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getLastUsedAt() { return lastUsedAt; }
    public void setLastUsedAt(LocalDateTime lastUsedAt) { this.lastUsedAt = lastUsedAt; }
    
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}