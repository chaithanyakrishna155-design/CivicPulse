package com.civicpulse.repository;


import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintAssignment;
import com.civicpulse.entity.User;



@Repository
public interface ComplaintAssignmentRepository 
        extends JpaRepository<ComplaintAssignment, Long> {





    // ==========================
    // Find By Officer
    // ==========================


    List<ComplaintAssignment> findByOfficer(
            User officer
    );



    @Query("""
            SELECT ca
            FROM ComplaintAssignment ca
            JOIN FETCH ca.complaint c
            JOIN FETCH ca.officer o
            WHERE o = :officer
            ORDER BY ca.assignedAt DESC
            """)
    List<ComplaintAssignment> findByOfficerOrderByAssignedAtDesc(
            @Param("officer") User officer
    );



    List<ComplaintAssignment> findByOfficerOrderByStatusAscAssignedAtDesc(
            User officer
    );



    List<ComplaintAssignment> findByOfficerOrderByAssignedAtDesc(
            User officer,
            Pageable pageable
    );







    // ==========================
    // Find By Complaint
    // ==========================


    Optional<ComplaintAssignment> findByComplaint(
            Complaint complaint
    );



    Optional<ComplaintAssignment> findByComplaintId(
            Long complaintId
    );






    // ==========================
    // Find By Status
    // ==========================


    List<ComplaintAssignment> findByStatus(
            String status
    );



    List<ComplaintAssignment> findByStatusOrderByAssignedAtDesc(
            String status
    );



    List<ComplaintAssignment> findByOfficerAndStatus(
            User officer,
            String status
    );







    // ==========================
    // Active Assignments
    // ==========================


    @Query("""
            SELECT ca
            FROM ComplaintAssignment ca
            JOIN FETCH ca.complaint c
            WHERE ca.officer = :officer
            AND ca.status <> 'RESOLVED'
            ORDER BY ca.assignedAt DESC
            """)
    List<ComplaintAssignment> findActiveAssignmentsByOfficer(
            @Param("officer") User officer
    );




    @Query("""
            SELECT ca
            FROM ComplaintAssignment ca
            JOIN FETCH ca.complaint c
            WHERE ca.status <> 'RESOLVED'
            ORDER BY ca.assignedAt DESC
            """)
    List<ComplaintAssignment> findAllActiveAssignments();








    // ==========================
    // Overdue Complaints
    // ==========================


    @Query("""
            SELECT ca
            FROM ComplaintAssignment ca
            WHERE ca.status <> 'RESOLVED'
            AND ca.assignedAt < :date
            """)
    List<ComplaintAssignment> findOverdueAssignments(
            @Param("date")
            LocalDateTime date
    );









    // ==========================
    // Count Queries
    // ==========================


    long countByOfficer(
            User officer
    );



    long countByOfficerAndStatus(
            User officer,
            String status
    );



    long countByOfficerAndStatusNot(
            User officer,
            String status
    );



    long countByStatus(
            String status
    );







    // ==========================
    // Exists Check
    // ==========================


    boolean existsByComplaintId(
            Long complaintId
    );



    boolean existsByComplaintAndStatus(
            Complaint complaint,
            String status
    );








    // ==========================
    // Delete Operations
    // ==========================


    @Transactional
    void deleteByComplaintId(
            Long complaintId
    );




    @Modifying
    @Transactional
    @Query("""
            DELETE FROM ComplaintAssignment ca
            WHERE ca.complaint.id = :complaintId
            """)
    void deleteAssignmentByComplaintId(
            @Param("complaintId")
            Long complaintId
    );








    // ==========================
    // Update Status
    // ==========================


    @Modifying
    @Transactional
    @Query("""
            UPDATE ComplaintAssignment ca
            SET ca.status = :status
            WHERE ca.id = :id
            """)
    void updateStatus(
            @Param("id")
            Long assignmentId,

            @Param("status")
            String status
    );









    // ==========================
    // Dashboard Statistics
    // ==========================


    @Query("""
            SELECT COUNT(ca)
            FROM ComplaintAssignment ca
            WHERE ca.status = 'RESOLVED'
            """)
    long countResolved();




    @Query("""
            SELECT COUNT(ca)
            FROM ComplaintAssignment ca
            WHERE ca.status = 'IN PROGRESS'
            """)
    long countInProgress();




    @Query("""
            SELECT COUNT(ca)
            FROM ComplaintAssignment ca
            WHERE ca.status = 'ASSIGNED'
            """)
    long countAssigned();







    @Query("""
            SELECT ca.status, COUNT(ca)
            FROM ComplaintAssignment ca
            GROUP BY ca.status
            """)
    List<Object[]> countByStatusGroup();









    // ==========================
    // Recent Assignments
    // ==========================


    List<ComplaintAssignment> findTop10ByOrderByAssignedAtDesc();

}