package com.civicpulse.repository;

import com.civicpulse.entity.NotificationPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationPreferenceRepository extends JpaRepository<NotificationPreference, Long> {
    
    // ===== Find by User ID =====
    Optional<NotificationPreference> findByUserId(String userId);
    
    List<NotificationPreference> findAllByUserIdIn(List<String> userIds);
    
    // ===== Check Existence =====
    boolean existsByUserId(String userId);
    
    // ===== Delete =====
    void deleteByUserId(String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM NotificationPreference np WHERE np.userId = :userId")
    void deleteByUserIdQuery(@Param("userId") String userId);
    
    // ===== Find Users with Specific Preferences =====
    @Query("SELECT np.userId FROM NotificationPreference np WHERE np.emailEnabled = true")
    List<String> findUsersWithEmailEnabled();
    
    @Query("SELECT np.userId FROM NotificationPreference np WHERE np.smsEnabled = true")
    List<String> findUsersWithSmsEnabled();
    
    @Query("SELECT np.userId FROM NotificationPreference np WHERE np.pushEnabled = true")
    List<String> findUsersWithPushEnabled();
    
    // ===== Find Users for Specific Events =====
    @Query("SELECT np.userId FROM NotificationPreference np " +
           "WHERE np.emailEnabled = true AND np.complaintUpdates = true")
    List<String> findUsersForComplaintUpdatesEmail();
    
    @Query("SELECT np.userId FROM NotificationPreference np " +
           "WHERE np.pushEnabled = true AND np.assignmentNotifications = true")
    List<String> findUsersForAssignmentNotificationsPush();
    
    @Query("SELECT np.userId FROM NotificationPreference np " +
           "WHERE np.smsEnabled = true AND np.resolutionNotifications = true")
    List<String> findUsersForResolutionNotificationsSms();
    
    @Query("SELECT np.userId FROM NotificationPreference np " +
           "WHERE (np.emailEnabled = true OR np.smsEnabled = true OR np.pushEnabled = true) " +
           "AND (np.complaintUpdates = true OR np.assignmentNotifications = true OR np.resolutionNotifications = true)")
    List<String> findUsersWithAnyNotificationEnabled();
    
    // ===== Count Statistics =====
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.emailEnabled = true")
    long countEmailEnabled();
    
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.smsEnabled = true")
    long countSmsEnabled();
    
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.pushEnabled = true")
    long countPushEnabled();
    
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.emailEnabled = false AND np.smsEnabled = false AND np.pushEnabled = false")
    long countAllDisabled();
    
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.emailEnabled = true OR np.smsEnabled = true OR np.pushEnabled = true")
    long countAnyEnabled();
    
    // ===== Update Operations =====
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET np.emailEnabled = :enabled WHERE np.userId = :userId")
    void updateEmailEnabled(@Param("userId") String userId, @Param("enabled") boolean enabled);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET np.smsEnabled = :enabled WHERE np.userId = :userId")
    void updateSmsEnabled(@Param("userId") String userId, @Param("enabled") boolean enabled);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET np.pushEnabled = :enabled WHERE np.userId = :userId")
    void updatePushEnabled(@Param("userId") String userId, @Param("enabled") boolean enabled);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET " +
           "np.emailEnabled = :email, " +
           "np.smsEnabled = :sms, " +
           "np.pushEnabled = :push " +
           "WHERE np.userId = :userId")
    void updateAllChannels(@Param("userId") String userId,
                           @Param("email") boolean email,
                           @Param("sms") boolean sms,
                           @Param("push") boolean push);
    
    // ===== Batch Operations =====
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET np.emailEnabled = false WHERE np.userId IN :userIds")
    void disableEmailForUsers(@Param("userIds") List<String> userIds);
    
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET np.smsEnabled = true WHERE np.userId IN :userIds")
    void enableSmsForUsers(@Param("userIds") List<String> userIds);
    
    // ===== Count by Preference Combination =====
    @Query("SELECT COUNT(np) FROM NotificationPreference np WHERE np.emailEnabled = :email AND np.smsEnabled = :sms AND np.pushEnabled = :push")
    long countByChannelCombination(@Param("email") boolean email,
                                   @Param("sms") boolean sms,
                                   @Param("push") boolean push);
    
    // ===== Default Preferences =====
    @Modifying
    @Transactional
    @Query("UPDATE NotificationPreference np SET " +
           "np.emailEnabled = true, " +
           "np.smsEnabled = false, " +
           "np.pushEnabled = true, " +
           "np.complaintUpdates = true, " +
           "np.assignmentNotifications = true, " +
           "np.resolutionNotifications = true " +
           "WHERE np.userId = :userId")
    void resetToDefault(@Param("userId") String userId);
}