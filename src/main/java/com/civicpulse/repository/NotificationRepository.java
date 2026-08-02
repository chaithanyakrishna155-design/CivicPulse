package com.civicpulse.repository;

import com.civicpulse.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    // ===== Find by User =====
    List<Notification> findByUserIdOrderByCreatedAtDesc(String userId);
    
    List<Notification> findByUserIdAndReadFalseOrderByCreatedAtDesc(String userId);
    
    List<Notification> findByUserIdAndReadTrueOrderByCreatedAtDesc(String userId);
    
    List<Notification> findTop10ByUserIdOrderByCreatedAtDesc(String userId);
    
    long countByUserIdAndReadFalse(String userId);
    
    long countByUserId(String userId);
    
    // ===== Find by Status =====
    List<Notification> findByStatus(String status);
    
    List<Notification> findByStatusOrderByCreatedAtDesc(String status);
    
    long countByStatus(String status);
    
    // ===== Find by Type =====
    List<Notification> findByType(String type);
    
    List<Notification> findByTypeOrderByCreatedAtDesc(String type);
    
    long countByType(String type);
    
    // ===== Find by Date Range =====
    List<Notification> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    List<Notification> findByUserIdAndCreatedAtBetween(String userId, LocalDateTime start, LocalDateTime end);
    
    long countByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    @Query("SELECT n FROM Notification n WHERE n.createdAt >= :since")
    List<Notification> findRecentNotifications(@Param("since") LocalDateTime since);
    
    // ===== Find by Status and User =====
    List<Notification> findByUserIdAndStatus(String userId, String status);
    
    long countByUserIdAndStatus(String userId, String status);
    
    // ===== Find Unread by Type =====
    List<Notification> findByUserIdAndReadFalseAndType(String userId, String type);
    
    long countByUserIdAndReadFalseAndType(String userId, String type);
    
    // ===== Mark Operations =====
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.id = :notificationId")
    void markAsRead(@Param("userId") String userId, @Param("notificationId") Long notificationId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId")
    void markAllAsRead(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = false WHERE n.userId = :userId AND n.id = :notificationId")
    void markAsUnread(@Param("userId") String userId, @Param("notificationId") Long notificationId);
    
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.read = true WHERE n.userId = :userId AND n.type = :type")
    void markAllAsReadByType(@Param("userId") String userId, @Param("type") String type);
    
    // ===== Delete Operations =====
    @Modifying
    @Transactional
    void deleteByUserId(String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.read = true")
    void deleteReadNotifications(@Param("userId") String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    void deleteOldNotifications(@Param("date") LocalDateTime date);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.userId = :userId AND n.createdAt < :date")
    void deleteOldUserNotifications(@Param("userId") String userId, @Param("date") LocalDateTime date);
    
    // ===== Statistics =====
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.read = false")
    long countAllUnread();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'SENT'")
    long countSent();
    
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.status = 'FAILED'")
    long countFailed();
    
    @Query("SELECT n.type, COUNT(n) FROM Notification n GROUP BY n.type")
    List<Object[]> countByTypeGroup();
    
    @Query("SELECT n.status, COUNT(n) FROM Notification n GROUP BY n.status")
    List<Object[]> countByStatusGroup();
    
    @Query("SELECT DATE(n.createdAt), COUNT(n) FROM Notification n " +
           "WHERE n.createdAt >= :startDate GROUP BY DATE(n.createdAt)")
    List<Object[]> getDailyNotificationCounts(@Param("startDate") LocalDateTime startDate);
    
    // ===== Find by Recipient =====
    List<Notification> findByRecipientContainingIgnoreCase(String recipient);
    
    // ===== Find with Link =====
    List<Notification> findByLinkIsNotNull();
    
    // ===== Batch Update Status =====
    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.status = :status WHERE n.id IN :ids")
    void updateStatusForNotifications(@Param("ids") List<Long> ids, @Param("status") String status);
    
    // ===== Find Pending Notifications =====
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' ORDER BY n.createdAt ASC")
    List<Notification> findPendingNotifications();
    
    @Query("SELECT n FROM Notification n WHERE n.status = 'PENDING' AND n.createdAt < :timeout")
    List<Notification> findTimeoutNotifications(@Param("timeout") LocalDateTime timeout);
}