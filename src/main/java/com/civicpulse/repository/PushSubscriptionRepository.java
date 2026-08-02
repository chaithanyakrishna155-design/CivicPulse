package com.civicpulse.repository;

import com.civicpulse.entity.PushSubscription;
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
public interface PushSubscriptionRepository extends JpaRepository<PushSubscription, Long> {
    
    // ===== Find by User =====
    List<PushSubscription> findByUserId(String userId);
    
    List<PushSubscription> findByUserIdAndActiveTrue(String userId);
    
    long countByUserId(String userId);
    
    // ===== Find by Endpoint =====
    Optional<PushSubscription> findByEndpoint(String endpoint);
    
    boolean existsByUserIdAndEndpoint(String userId, String endpoint);
    
    // ===== Find by Active Status =====
    List<PushSubscription> findByActiveTrue();
    
    List<PushSubscription> findByActiveFalse();
    
    // ===== Delete Operations =====
    void deleteByUserIdAndEndpoint(String userId, String endpoint);
    
    void deleteByUserId(String userId);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PushSubscription ps WHERE ps.active = false")
    void deleteInactiveSubscriptions();
    
    @Modifying
    @Transactional
    @Query("DELETE FROM PushSubscription ps WHERE ps.userId = :userId AND ps.active = false")
    void deleteInactiveUserSubscriptions(@Param("userId") String userId);
    
    // ===== Update Operations =====
    @Modifying
    @Transactional
    @Query("UPDATE PushSubscription ps SET ps.active = false WHERE ps.id = :id")
    void deactivateSubscription(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE PushSubscription ps SET ps.active = true WHERE ps.id = :id")
    void activateSubscription(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE PushSubscription ps SET ps.lastUsedAt = CURRENT_TIMESTAMP WHERE ps.id = :id")
    void updateLastUsed(@Param("id") Long id);
    
    @Modifying
    @Transactional
    @Query("UPDATE PushSubscription ps SET ps.active = false WHERE ps.userId = :userId")
    void deactivateAllUserSubscriptions(@Param("userId") String userId);
    
    // ===== Statistics =====
    @Query("SELECT COUNT(ps) FROM PushSubscription ps WHERE ps.active = true")
    long countActiveSubscriptions();
    
    @Query("SELECT COUNT(ps) FROM PushSubscription ps WHERE ps.active = false")
    long countInactiveSubscriptions();
    
    // ===== Check if user has active subscription =====
    @Query("SELECT COUNT(ps) > 0 FROM PushSubscription ps WHERE ps.userId = :userId AND ps.active = true")
    boolean hasActiveSubscription(@Param("userId") String userId);
    
    // ===== Find users with active subscriptions =====
    @Query("SELECT DISTINCT ps.userId FROM PushSubscription ps WHERE ps.active = true")
    List<String> findUsersWithActiveSubscriptions();
}