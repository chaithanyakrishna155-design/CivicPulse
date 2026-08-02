package com.civicpulse.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.civicpulse.entity.OfficerRequest;

@Repository
public interface OfficerRequestRepository 
        extends JpaRepository<OfficerRequest, Long> {

    // ===== Find by Email =====
    Optional<OfficerRequest> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    // ===== Find by Status =====
    List<OfficerRequest> findByStatus(String status);
    
    List<OfficerRequest> findByStatusOrderByRequestedAtDesc(String status);
    
    long countByStatus(String status);
    
    // ===== Find Pending Requests =====
    @Query("SELECT or FROM OfficerRequest or WHERE or.status = 'PENDING' ORDER BY or.requestedAt ASC")
    List<OfficerRequest> findPendingRequests();
    
    @Query("SELECT or FROM OfficerRequest or WHERE or.status = 'PENDING' AND or.requestedAt < :cutoffDate")
    List<OfficerRequest> findPendingRequestsOlderThan(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    // ===== Find Approved/Rejected =====
    List<OfficerRequest> findByApprovedTrue();
    
    List<OfficerRequest> findByApprovedFalse();
    
    List<OfficerRequest> findByApprovedTrueOrderByApprovedAtDesc();
    
    List<OfficerRequest> findByApprovedFalseAndStatusNot(String status);
    
    // ===== Find by Email Domain =====
    @Query("SELECT or FROM OfficerRequest or WHERE or.email LIKE CONCAT('%', :domain)")
    List<OfficerRequest> findByEmailDomain(@Param("domain") String domain);
    
    // ===== Find Recent Requests =====
    List<OfficerRequest> findTop10ByOrderByRequestedAtDesc();
    
    List<OfficerRequest> findTop10ByStatusOrderByRequestedAtDesc(String status);
    
    // ===== Statistics =====
    @Query("SELECT COUNT(or) FROM OfficerRequest or WHERE or.status = 'PENDING'")
    long countPending();
    
    @Query("SELECT COUNT(or) FROM OfficerRequest or WHERE or.status = 'APPROVED'")
    long countApproved();
    
    @Query("SELECT COUNT(or) FROM OfficerRequest or WHERE or.status = 'REJECTED'")
    long countRejected();
    
    @Query("SELECT or.status, COUNT(or) FROM OfficerRequest or GROUP BY or.status")
    List<Object[]> countByStatusGroup();
    
    @Query("SELECT DATE(or.requestedAt), COUNT(or) FROM OfficerRequest or " +
           "WHERE or.requestedAt >= :startDate GROUP BY DATE(or.requestedAt)")
    List<Object[]> getDailyRequestCounts(@Param("startDate") java.time.LocalDateTime startDate);
    
    // ===== Update Operations =====
    @Modifying
    @Transactional
    @Query("UPDATE OfficerRequest or SET or.status = 'APPROVED', or.approved = true, " +
           "or.approvedAt = CURRENT_TIMESTAMP, or.approvedBy = :approvedBy " +
           "WHERE or.id = :id")
    void approveRequest(@Param("id") Long id, @Param("approvedBy") String approvedBy);
    
    @Modifying
    @Transactional
    @Query("UPDATE OfficerRequest or SET or.status = 'REJECTED', or.approved = false, " +
           "or.rejectedAt = CURRENT_TIMESTAMP, or.rejectionReason = :reason, " +
           "or.approvedBy = :approvedBy " +
           "WHERE or.id = :id")
    void rejectRequest(@Param("id") Long id, 
                       @Param("approvedBy") String approvedBy, 
                       @Param("reason") String reason);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM OfficerRequest or WHERE or.status = 'REJECTED' AND or.rejectedAt < :cutoffDate")
    void deleteOldRejectedRequests(@Param("cutoffDate") java.time.LocalDateTime cutoffDate);
    
    // ===== Search =====
    @Query("SELECT or FROM OfficerRequest or WHERE LOWER(or.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<OfficerRequest> searchRequests(@Param("keyword") String keyword);
    
    @Query("SELECT or FROM OfficerRequest or WHERE LOWER(or.email) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "AND or.status = :status")
    List<OfficerRequest> searchRequestsByStatus(@Param("keyword") String keyword, 
                                                @Param("status") String status);
    
    // ===== Find with Notes =====
    @Query("SELECT or FROM OfficerRequest or WHERE or.notes IS NOT NULL AND or.notes != ''")
    List<OfficerRequest> findRequestsWithNotes();
    
    // ===== Find by Approver =====
    List<OfficerRequest> findByApprovedBy(String approvedBy);
    
    List<OfficerRequest> findByApprovedByOrderByApprovedAtDesc(String approvedBy);
    
    // ===== Check if email is already approved =====
    @Query("SELECT COUNT(or) > 0 FROM OfficerRequest or WHERE or.email = :email AND or.status = 'APPROVED'")
    boolean isEmailApproved(@Param("email") String email);
    
    @Query("SELECT COUNT(or) > 0 FROM OfficerRequest or WHERE or.email = :email AND or.status = 'PENDING'")
    boolean isEmailPending(@Param("email") String email);
}