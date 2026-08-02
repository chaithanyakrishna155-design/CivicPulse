package com.civicpulse.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "officer_requests")
public class OfficerRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "status")
    private String status = "PENDING"; // PENDING, APPROVED, REJECTED

    @Column(name = "approved")
    private boolean approved = false;

    @Column(name = "requested_at", nullable = false, updatable = false)
    @CreationTimestamp
    private LocalDateTime requestedAt;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "rejected_at")
    private LocalDateTime rejectedAt;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "approved_by")
    private String approvedBy;

    @Column(name = "notes", length = 500)
    private String notes;

    // ===== Constructors =====
    public OfficerRequest() {
        this.status = "PENDING";
        this.approved = false;
    }

    public OfficerRequest(String email) {
        this.email = email;
        this.status = "PENDING";
        this.approved = false;
    }

    // ===== Lifecycle Callbacks =====
    @PrePersist
    protected void onCreate() {
        if (status == null) {
            status = "PENDING";
        }
        if (requestedAt == null) {
            requestedAt = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        // Auto-update timestamps based on status
        if ("APPROVED".equals(status) && approvedAt == null) {
            this.approvedAt = LocalDateTime.now();
            this.approved = true;
        } else if ("REJECTED".equals(status) && rejectedAt == null) {
            this.rejectedAt = LocalDateTime.now();
            this.approved = false;
        }
    }

    // ===== Getters and Setters =====
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        if (status != null) {
            String normalized = status.toUpperCase();
            if (!normalized.equals("PENDING") && !normalized.equals("APPROVED") && !normalized.equals("REJECTED")) {
                throw new IllegalArgumentException("Status must be PENDING, APPROVED, or REJECTED");
            }
            this.status = normalized;
            
            // Auto-set timestamps based on status
            if (normalized.equals("APPROVED")) {
                this.approved = true;
                this.approvedAt = LocalDateTime.now();
            } else if (normalized.equals("REJECTED")) {
                this.approved = false;
                this.rejectedAt = LocalDateTime.now();
            }
        }
    }

    public boolean isApproved() {
        return approved;
    }

    public void setApproved(boolean approved) {
        this.approved = approved;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public void setRequestedAt(LocalDateTime requestedAt) {
        this.requestedAt = requestedAt;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public LocalDateTime getRejectedAt() {
        return rejectedAt;
    }

    public void setRejectedAt(LocalDateTime rejectedAt) {
        this.rejectedAt = rejectedAt;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    // ===== Helper Methods =====
    public boolean isPending() {
        return "PENDING".equalsIgnoreCase(status);
    }

    public boolean isApprovedStatus() {
        return "APPROVED".equalsIgnoreCase(status);
    }

    public boolean isRejected() {
        return "REJECTED".equalsIgnoreCase(status);
    }

    public boolean canBeApproved() {
        return isPending() && !approved;
    }

    public boolean canBeRejected() {
        return isPending() && !approved;
    }

    public String getStatusDisplay() {
        if (isPending()) return "🟡 PENDING";
        if (isApprovedStatus()) return "🟢 APPROVED";
        if (isRejected()) return "🔴 REJECTED";
        return status;
    }

    public String getStatusBadgeClass() {
        if (isPending()) return "badge bg-warning text-dark";
        if (isApprovedStatus()) return "badge bg-success";
        if (isRejected()) return "badge bg-danger";
        return "badge bg-secondary";
    }

    public long getTimeSinceRequest() {
        if (requestedAt == null) return 0;
        return java.time.Duration.between(requestedAt, LocalDateTime.now()).toHours();
    }

    public boolean isOverdueForReview() {
        // Consider overdue if pending for more than 7 days
        return isPending() && getTimeSinceRequest() > 168; // 7 days
    }

    // ===== Approval Method =====
    public void approve(String approvedBy) {
        this.approved = true;
        this.status = "APPROVED";
        this.approvedBy = approvedBy;
        this.approvedAt = LocalDateTime.now();
        this.rejectedAt = null;
        this.rejectionReason = null;
    }

    // ===== Rejection Method =====
    public void reject(String approvedBy, String reason) {
        this.approved = false;
        this.status = "REJECTED";
        this.approvedBy = approvedBy;
        this.rejectedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.approvedAt = null;
    }

    // ===== toString for debugging =====
    @Override
    public String toString() {
        return "OfficerRequest{" +
                "id=" + id +
                ", email='" + email + '\'' +
                ", status='" + status + '\'' +
                ", approved=" + approved +
                ", requestedAt=" + requestedAt +
                ", approvedAt=" + approvedAt +
                '}';
    }

    // ===== equals and hashCode =====
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OfficerRequest that = (OfficerRequest) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}