package com.civicpulse.entity;

import jakarta.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;


@Entity
@Table(name = "complaint_assignments")
public class ComplaintAssignment {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;



    // UPDATED: EAGER loading to show complaint details in officer dashboard
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "complaint_id",
            nullable = false
    )
    private Complaint complaint;



    // UPDATED: EAGER loading for officer details
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(
            name = "officer_id",
            nullable = false
    )
    private User officer;



    @Column(nullable = false)
    private String status;



    @CreationTimestamp
    @Column(
            name = "assigned_at",
            nullable = true,
            updatable = false
    )
    private LocalDateTime assignedAt;



    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    @Column(name = "started_at")
    private LocalDateTime startedAt;



    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;



    @Column(name = "assigned_by")
    private String assignedBy;



    @Column(name = "resolution_notes", length = 500)
    private String resolutionNotes;



    @Column(length = 500)
    private String feedback;



    @Column
    private Integer rating;



    @Column(name = "is_completed")
    private boolean completed = false;

    public ComplaintAssignment() {

    }



    public ComplaintAssignment(
            Complaint complaint,
            User officer
    ) {

        this.complaint = complaint;
        this.officer = officer;
        this.status = "ASSIGNED";

    }





    @PrePersist
    public void onCreate() {


        if(status == null || status.isEmpty()) {

            status = "ASSIGNED";

        }


        if(assignedAt == null) {

            assignedAt = LocalDateTime.now();

        }


        if(updatedAt == null) {

            updatedAt = LocalDateTime.now();

        }

    }





    @PreUpdate
    public void onUpdate() {

        updatedAt = LocalDateTime.now();

    }





    public Long getId() {

        return id;

    }



    public void setId(Long id) {

        this.id = id;

    }




    public Complaint getComplaint() {

        return complaint;

    }



    public void setComplaint(Complaint complaint) {

        this.complaint = complaint;

    }





    public User getOfficer() {

        return officer;

    }



    public void setOfficer(User officer) {

        this.officer = officer;

    }





    public String getStatus() {

        return status;

    }




    public void setStatus(String status) {

        this.status = status;


        if("IN PROGRESS".equalsIgnoreCase(status)
                && startedAt == null) {

            startedAt = LocalDateTime.now();

        }



        if("RESOLVED".equalsIgnoreCase(status)
                && resolvedAt == null) {

            resolvedAt = LocalDateTime.now();

            completed = true;

        }

    }
    public LocalDateTime getAssignedAt() {

        return assignedAt;

    }



    public void setAssignedAt(LocalDateTime assignedAt) {

        this.assignedAt = assignedAt;

    }




    public LocalDateTime getUpdatedAt() {

        return updatedAt;

    }



    public void setUpdatedAt(LocalDateTime updatedAt) {

        this.updatedAt = updatedAt;

    }




    public LocalDateTime getStartedAt() {

        return startedAt;

    }



    public void setStartedAt(LocalDateTime startedAt) {

        this.startedAt = startedAt;

    }




    public LocalDateTime getResolvedAt() {

        return resolvedAt;

    }



    public void setResolvedAt(LocalDateTime resolvedAt) {

        this.resolvedAt = resolvedAt;

    }




    public String getAssignedBy() {

        return assignedBy;

    }



    public void setAssignedBy(String assignedBy) {

        this.assignedBy = assignedBy;

    }




    public String getResolutionNotes() {

        return resolutionNotes;

    }



    public void setResolutionNotes(String resolutionNotes) {

        this.resolutionNotes = resolutionNotes;

    }




    public String getFeedback() {

        return feedback;

    }



    public void setFeedback(String feedback) {

        this.feedback = feedback;

    }




    public Integer getRating() {

        return rating;

    }



    public void setRating(Integer rating) {


        if(rating != null &&
                (rating < 1 || rating > 5)) {


            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5"
            );

        }


        this.rating = rating;

    }




    public boolean isCompleted() {

        return completed;

    }



    public void setCompleted(boolean completed) {

        this.completed = completed;

    }





    // ==========================
    // STATUS CHECK METHODS
    // ==========================


    public boolean isAssigned() {

        return "ASSIGNED"
                .equalsIgnoreCase(status);

    }




    public boolean isInProgress() {

        return "IN PROGRESS"
                .equalsIgnoreCase(status);

    }




    public boolean isResolved() {

        return "RESOLVED"
                .equalsIgnoreCase(status);

    }





    public String getDisplayStatus() {


        if(isAssigned())

            return "📌 Assigned";



        if(isInProgress())

            return "🔄 In Progress";



        if(isResolved())

            return "✅ Resolved";



        return status;

    }





    public String getStatusBadgeClass() {


        if(isAssigned())

            return "badge bg-warning text-dark";



        if(isInProgress())

            return "badge bg-info text-white";



        if(isResolved())

            return "badge bg-success text-white";



        return "badge bg-secondary";

    }
    // ==========================
    // TIME CALCULATIONS
    // ==========================


    public long getTimeSinceAssignment() {


        if(assignedAt == null)

            return 0;



        return Duration.between(
                assignedAt,
                LocalDateTime.now()
        ).toHours();

    }





    public long getTimeToResolve() {


        if(assignedAt == null ||
                resolvedAt == null)

            return 0;



        return Duration.between(
                assignedAt,
                resolvedAt
        ).toHours();

    }





    public boolean isOverdue() {


        return !isResolved()
                &&
                getTimeSinceAssignment() > 72;

    }





    // ==========================
    // HELPER METHODS
    // ==========================


    public String getOfficerName() {


        if(officer == null)

            return "Not Assigned";


        return officer.getFullName();

    }





    public String getComplaintTitle() {


        if(complaint == null)

            return "No Complaint";


        return complaint.getTitle();

    }





    public String getComplaintCategory() {


        if(complaint == null)

            return "";


        return complaint.getCategory();

    }





    public String getComplaintLocation() {


        if(complaint == null)

            return "";


        return complaint.getLocation();

    }





    public String getComplaintDescription() {


        if(complaint == null)

            return "";


        return complaint.getDescription();

    }





    // ==========================
    // EQUALS HASHCODE TOSTRING
    // ==========================


    @Override
    public boolean equals(Object o) {


        if(this == o)

            return true;



        if(!(o instanceof ComplaintAssignment))

            return false;



        ComplaintAssignment that =
                (ComplaintAssignment)o;



        return Objects.equals(
                id,
                that.id
        );

    }





    @Override
    public int hashCode() {


        return Objects.hash(id);

    }





    @Override
    public String toString() {


        return "ComplaintAssignment{" +

                "id=" + id +

                ", status='" + status + '\'' +

                ", assignedAt=" + assignedAt +

                ", officer=" +
                (officer != null ?
                        officer.getEmail()
                        :
                        "null") +

                '}';

    }



}