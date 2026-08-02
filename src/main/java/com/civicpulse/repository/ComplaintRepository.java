package com.civicpulse.repository;

import com.civicpulse.entity.Complaint;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Repository
public interface ComplaintRepository 
        extends JpaRepository<Complaint, Long> {



    // ==========================
    // STATUS
    // ==========================

    List<Complaint> findByStatus(String status);


    Page<Complaint> findByStatus(
            String status,
            Pageable pageable
    );



    // ==========================
    // USER
    // ==========================

    List<Complaint> findByUser_Id(Long userId);


    Page<Complaint> findByUser_Id(
            Long userId,
            Pageable pageable
    );



    // ==========================
    // OFFICER
    // ==========================

    List<Complaint> findByAssignedOfficer(
            String officerEmail
    );



    // ==========================
    // SEARCH
    // ==========================

    @Query("""
        SELECT c FROM Complaint c
        WHERE LOWER(c.title)
        LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(c.description)
        LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(c.category)
        LIKE LOWER(CONCAT('%',:keyword,'%'))
        OR LOWER(c.location)
        LIKE LOWER(CONCAT('%',:keyword,'%'))
        """)
    Page<Complaint> searchComplaints(
            @Param("keyword") String keyword,
            Pageable pageable
    );



    // ==========================
    // CATEGORY
    // ==========================

    List<Complaint> findByCategory(
            String category
    );


    @Query("""
        SELECT DISTINCT c.category
        FROM Complaint c
        """)
    List<String> findDistinctCategories();




    // ==========================
    // FILTER
    // ==========================

    @Query("""
        SELECT c FROM Complaint c
        WHERE
        (:status IS NULL OR c.status=:status)
        AND
        (:category IS NULL OR c.category=:category)
        AND
        (:location IS NULL OR
        LOWER(c.location)
        LIKE LOWER(CONCAT('%',:location,'%')))
        """)
    Page<Complaint> filterComplaints(
            @Param("status") String status,
            @Param("category") String category,
            @Param("location") String location,
            Pageable pageable
    );



    // ==========================
    // DASHBOARD
    // ==========================


    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        """)
    long getTotalComplaintsCount();



    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        WHERE c.status='Pending'
        """)
    long getPendingCount();



    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        WHERE c.status='Assigned'
        """)
    long getAssignedCount();



    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        WHERE c.status='In Progress'
        """)
    long getInProgressCount();



    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        WHERE c.status='Resolved'
        """)
    long getResolvedCount();




    @Query("""
        SELECT c.status, COUNT(c)
        FROM Complaint c
        GROUP BY c.status
        """)
    List<Object[]> countByStatusGroup();




    // ==========================
    // EVIDENCE
    // ==========================

    @Query("""
        SELECT c FROM Complaint c
        WHERE c.photoName IS NOT NULL
        OR c.videoName IS NOT NULL
        """)
    List<Complaint> findComplaintsWithEvidence();



    @Query("""
        SELECT COUNT(c)
        FROM Complaint c
        WHERE c.photoName IS NOT NULL
        OR c.videoName IS NOT NULL
        """)
    long countComplaintsWithEvidence();




    // ==========================
    // BULK UPDATE
    // ==========================


    @Modifying
    @Transactional
    @Query("""
        UPDATE Complaint c
        SET c.status=:status
        WHERE c.id IN :ids
        """)
    void bulkUpdateStatus(
            @Param("ids") List<Long> ids,
            @Param("status") String status
    );



    @Modifying
    @Transactional
    @Query("""
        UPDATE Complaint c
        SET c.assignedOfficer=:officer
        WHERE c.id IN :ids
        """)
    void bulkAssignOfficer(
            @Param("ids") List<Long> ids,
            @Param("officer") String officer
    );

}