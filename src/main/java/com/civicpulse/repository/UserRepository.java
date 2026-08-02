package com.civicpulse.repository;

import com.civicpulse.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    // ===== Find by Email =====

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);



    // ===== Find by Role =====

    List<User> findByRole(String role);

    Page<User> findByRole(String role, Pageable pageable);

    long countByRole(String role);



    // ===== Find by Role and Status =====

    List<User> findByRoleAndApprovedTrue(String role);

    List<User> findByRoleAndApprovedFalse(String role);

    List<User> findByRoleAndActiveTrue(String role);

    List<User> findByRoleAndActiveFalse(String role);



    // ===== Find by Active Status =====

    List<User> findByActiveTrue();

    List<User> findByActiveFalse();

    long countByActiveTrue();

    long countByActiveFalse();



    // ===== Find by Approved Status =====

    List<User> findByApprovedTrue();

    List<User> findByApprovedFalse();

    long countByApprovedTrue();

    long countByApprovedFalse();



    // ===== Find Officers =====

    @Query("SELECT u FROM User u WHERE u.role = 'OFFICER' AND u.approved = true")
    List<User> findApprovedOfficers();


    @Query("SELECT u FROM User u WHERE u.role = 'OFFICER' AND u.approved = false")
    List<User> findPendingOfficers();


    @Query("SELECT u FROM User u WHERE u.role = 'OFFICER'")
    List<User> findAllOfficers();


    long countByRoleAndApprovedTrue(String role);




    // ===== Search Users =====

    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.phone) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    List<User> searchUsers(@Param("keyword") String keyword);



    @Query("""
           SELECT u FROM User u
           WHERE LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%'))
           """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            Pageable pageable
    );




    // ===== Find by Name =====

    List<User> findByFullNameContainingIgnoreCase(String name);


    List<User> findByFullNameContainingIgnoreCaseAndRole(
            String name,
            String role
    );




    // ===== Find by Phone =====

    Optional<User> findByPhone(String phone);


    boolean existsByPhone(String phone);




    // ===== Find by Last Login =====

    List<User> findByLastLoginBefore(LocalDateTime date);


    List<User> findByLastLoginBetween(
            LocalDateTime start,
            LocalDateTime end
    );


    @Query("""
           SELECT u FROM User u
           WHERE u.lastLogin IS NULL
           OR u.lastLogin < :cutoffDate
           """)
    List<User> findInactiveUsers(
            @Param("cutoffDate") LocalDateTime cutoffDate
    );




    // ===== Recent Users =====

    List<User> findTop10ByOrderByCreatedAtDesc();


    List<User> findTop10ByRoleOrderByCreatedAtDesc(String role);


    List<User> findByCreatedAtAfter(LocalDateTime date);




    // ===== Statistics =====

    @Query("SELECT COUNT(u) FROM User u")
    long getTotalUsers();



    @Query("""
           SELECT u.role, COUNT(u)
           FROM User u
           GROUP BY u.role
           """)
    List<Object[]> countByRoleGroup();



    @Query("""
           SELECT DATE(u.createdAt), COUNT(u)
           FROM User u
           WHERE u.createdAt >= :startDate
           GROUP BY DATE(u.createdAt)
           """)
    List<Object[]> getDailyUserRegistrations(
            @Param("startDate") LocalDateTime startDate
    );



    @Query("""
           SELECT MONTH(u.createdAt), COUNT(u)
           FROM User u
           WHERE u.createdAt >= :startDate
           GROUP BY MONTH(u.createdAt)
           """)
    List<Object[]> getMonthlyUserRegistrations(
            @Param("startDate") LocalDateTime startDate
    );




    // ===== Update Operations =====

    @Modifying
    @Transactional
    @Query("""
           UPDATE User u
           SET u.password = :password
           WHERE u.email = :email
           """)
    void updatePassword(
            @Param("email") String email,
            @Param("password") String password
    );



    @Modifying
    @Transactional
    @Query("""
           UPDATE User u
           SET u.role = :role
           WHERE u.id = :id
           """)
    void updateRole(
            @Param("id") Long id,
            @Param("role") String role
    );



    @Modifying
    @Transactional
    @Query("""
           UPDATE User u
           SET u.approved = :approved
           WHERE u.id = :id
           """)
    void updateApprovalStatus(
            @Param("id") Long id,
            @Param("approved") boolean approved
    );



    @Modifying
    @Transactional
    @Query("""
           UPDATE User u
           SET u.active = :active
           WHERE u.id = :id
           """)
    void updateActiveStatus(
            @Param("id") Long id,
            @Param("active") boolean active
    );



    @Modifying
    @Transactional
    @Query("""
           UPDATE User u
           SET u.lastLogin = CURRENT_TIMESTAMP
           WHERE u.id = :id
           """)
    void updateLastLogin(
            @Param("id") Long id
    );




    // ===== Delete Operations =====

    @Modifying
    @Transactional
    @Query("""
           DELETE FROM User u
           WHERE u.active = false
           AND u.createdAt < :cutoffDate
           """)
    void deleteInactiveUsers(
            @Param("cutoffDate") LocalDateTime cutoffDate
    );




    // ===== Find by Email Domain =====

    @Query("""
           SELECT u FROM User u
           WHERE u.email LIKE CONCAT('%', :domain)
           """)
    List<User> findByEmailDomain(
            @Param("domain") String domain
    );




    // ===== Find Users with No Complaints =====

    @Query("""
           SELECT u
           FROM User u
           WHERE u.id NOT IN
           (
               SELECT DISTINCT c.user.id
               FROM Complaint c
               WHERE c.user IS NOT NULL
           )
           """)
    List<User> findUsersWithNoComplaints();




    // ===== Find Users with Specific Role and Status =====

    @Query("""
           SELECT u FROM User u
           WHERE u.role = :role
           AND u.approved = :approved
           AND u.active = :active
           """)
    List<User> findByRoleAndApprovedAndActive(
            @Param("role") String role,
            @Param("approved") boolean approved,
            @Param("active") boolean active
    );

}