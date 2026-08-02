package com.civicpulse.service;

import com.civicpulse.entity.Notification;
import com.civicpulse.entity.NotificationPreference;
import com.civicpulse.entity.PushSubscription;
import com.civicpulse.repository.NotificationRepository;
import com.civicpulse.repository.NotificationPreferenceRepository;
import com.civicpulse.repository.PushSubscriptionRepository;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import jakarta.annotation.PostConstruct;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

@Service
public class NotificationService {
    
    private static final Logger log = LoggerFactory.getLogger(NotificationService.class);
    
    @Autowired
    private JavaMailSender emailSender;
    
    @Autowired
    private TemplateEngine templateEngine;
    
    @Autowired
    private NotificationRepository notificationRepository;
    
    @Autowired
    private PushSubscriptionRepository pushSubscriptionRepository;
    
    @Autowired
    private NotificationPreferenceRepository preferenceRepository;
    
    @Value("${twilio.account.sid:}")
    private String twilioSid;
    
    @Value("${twilio.auth.token:}")
    private String twilioToken;
    
    @Value("${twilio.phone.number:}")
    private String twilioPhoneNumber;
    
    @Value("${vapid.public.key:}")
    private String vapidPublicKey;
    
    @Value("${vapid.private.key:}")
    private String vapidPrivateKey;
    
    @PostConstruct
    public void initTwilio() {
        try {
            if (twilioSid != null && !twilioSid.isEmpty()) {
                Twilio.init(twilioSid, twilioToken);
                log.info("Twilio initialized successfully");
            }
        } catch (Exception e) {
            log.warn("Failed to initialize Twilio: {}", e.getMessage());
        }
    }
    
    // ==========================
    // EMAIL NOTIFICATIONS
    // ==========================
    public void sendEmailNotification(String userId, String to, String subject, 
                                      String templateName, Map<String, Object> data) {
        try {
            if (!shouldSendEmail(userId)) {
                log.info("Email notifications disabled for user: {}", userId);
                return;
            }
            
            Context context = new Context();
            context.setVariables(data);
            
            String htmlContent = templateEngine.process(templateName, context);
            
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            emailSender.send(message);
            log.info("Email sent to: {}", to);
            
            saveNotification(userId, subject, htmlContent, "EMAIL", to, "SENT");
            
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
            saveNotification(userId, subject, "Failed to send email", "EMAIL", to, "FAILED");
        }
    }
    
    // ==========================
    // SMS NOTIFICATIONS
    // ==========================
    public void sendSmsNotification(String userId, String phoneNumber, String message) {
        try {
            if (!shouldSendSms(userId)) {
                log.info("SMS notifications disabled for user: {}", userId);
                return;
            }
            
            if (twilioSid == null || twilioSid.isEmpty()) {
                log.warn("Twilio not configured. SMS not sent.");
                return;
            }
            
            Message sms = Message.creator(
                new PhoneNumber(phoneNumber),
                new PhoneNumber(twilioPhoneNumber),
                message
            ).create();
            
            log.info("SMS sent to {}: {}", phoneNumber, sms.getSid());
            saveNotification(userId, "SMS Notification", message, "SMS", phoneNumber, "SENT");
            
        } catch (Exception e) {
            log.error("Failed to send SMS to {}: {}", phoneNumber, e.getMessage());
            saveNotification(userId, "SMS Notification", "Failed to send SMS", "SMS", phoneNumber, "FAILED");
        }
    }
    
    // ==========================
    // PUSH NOTIFICATIONS
    // ==========================
    @Transactional
    public void sendPushNotification(String userId, String title, String body, Map<String, String> data) {
        try {
            if (!shouldSendPush(userId)) {
                log.info("Push notifications disabled for user: {}", userId);
                return;
            }
            
            List<PushSubscription> subscriptions = pushSubscriptionRepository.findByUserIdAndActiveTrue(userId);
            if (subscriptions.isEmpty()) {
                log.info("No active push subscriptions for user: {}", userId);
                return;
            }
            
            for (PushSubscription subscription : subscriptions) {
                try {
                    // Update last used timestamp
                    subscription.setLastUsedAt(LocalDateTime.now());
                    pushSubscriptionRepository.save(subscription);
                    
                } catch (Exception e) {
                    log.error("Failed to send push to endpoint: {}", subscription.getEndpoint(), e);
                    subscription.setActive(false);
                    pushSubscriptionRepository.save(subscription);
                }
            }
            
            saveNotification(userId, title, body, "PUSH", "Push Notification", "SENT");
            log.info("Push notification sent to user: {}", userId);
            
        } catch (Exception e) {
            log.error("Failed to send push notification to {}: {}", userId, e.getMessage());
            saveNotification(userId, title, "Failed to send push notification", "PUSH", userId, "FAILED");
        }
    }
    
    // ==========================
    // SAVE NOTIFICATION
    // ==========================
    @Transactional
    private void saveNotification(String userId, String title, String message, 
                                  String type, String recipient, String status) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setType(type);
        notification.setRecipient(recipient);
        notification.setStatus(status);
        notification.setCreatedAt(LocalDateTime.now());
        notification.setSentAt(LocalDateTime.now());
        notification.setRead(false);
        
        notificationRepository.save(notification);
    }
    
    // ==========================
    // GET NOTIFICATIONS
    // ==========================
    public List<Notification> getUserNotifications(String userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }
    
    public List<Notification> getUnreadNotifications(String userId) {
        return notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(userId);
    }
    
    public long getUnreadCount(String userId) {
        return notificationRepository.countByUserIdAndReadFalse(userId);
    }
    
    public long getTotalUnreadCount() {
        return notificationRepository.countAllUnread();
    }
    
    // ==========================
    // MARK AS READ
    // ==========================
    @Transactional
    public void markAsRead(String userId, Long notificationId) {
        notificationRepository.markAsRead(userId, notificationId);
    }
    
    @Transactional
    public void markAllAsRead(String userId) {
        notificationRepository.markAllAsRead(userId);
    }
    
    @Transactional
    public void markAsUnread(String userId, Long notificationId) {
        notificationRepository.markAsUnread(userId, notificationId);
    }
    
    // ==========================
    // DELETE NOTIFICATIONS
    // ==========================
    @Transactional
    public void deleteNotification(Long notificationId) {
        notificationRepository.deleteById(notificationId);
    }
    
    @Transactional
    public void deleteAllNotifications(String userId) {
        notificationRepository.deleteByUserId(userId);
    }
    
    @Transactional
    public void deleteReadNotifications(String userId) {
        notificationRepository.deleteReadNotifications(userId);
    }
    
    @Transactional
    public void deleteOldNotifications(LocalDateTime beforeDate) {
        notificationRepository.deleteOldNotifications(beforeDate);
    }
    
    // ==========================
    // PREFERENCES
    // ==========================
    @Transactional
    public NotificationPreference getPreferences(String userId) {
        return preferenceRepository.findByUserId(userId)
            .orElseGet(() -> {
                NotificationPreference pref = new NotificationPreference();
                pref.setUserId(userId);
                pref.setEmailEnabled(true);
                pref.setSmsEnabled(false);
                pref.setPushEnabled(true);
                pref.setComplaintUpdates(true);
                pref.setAssignmentNotifications(true);
                pref.setResolutionNotifications(true);
                return preferenceRepository.save(pref);
            });
    }
    
    @Transactional
    public void savePreferences(NotificationPreference preference) {
        preferenceRepository.save(preference);
        log.info("Preferences saved for user: {}", preference.getUserId());
    }
    
    @Transactional
    public void resetPreferences(String userId) {
        preferenceRepository.resetToDefault(userId);
        log.info("Preferences reset to default for user: {}", userId);
    }
    
    // ==========================
    // PUSH SUBSCRIPTION MANAGEMENT
    // ==========================
    @Transactional
    public void saveSubscription(PushSubscription subscription) {
        if (pushSubscriptionRepository.existsByUserIdAndEndpoint(
                subscription.getUserId(), subscription.getEndpoint())) {
            PushSubscription existing = pushSubscriptionRepository
                .findByEndpoint(subscription.getEndpoint()).orElse(null);
            if (existing != null) {
                existing.setP256dhKey(subscription.getP256dhKey());
                existing.setAuthKey(subscription.getAuthKey());
                existing.setActive(true);
                existing.setLastUsedAt(LocalDateTime.now());
                pushSubscriptionRepository.save(existing);
                return;
            }
        }
        
        subscription.setCreatedAt(LocalDateTime.now());
        subscription.setActive(true);
        pushSubscriptionRepository.save(subscription);
        log.info("Push subscription saved for user: {}", subscription.getUserId());
    }
    
    @Transactional
    public void removeSubscription(String userId, String endpoint) {
        pushSubscriptionRepository.deleteByUserIdAndEndpoint(userId, endpoint);
        log.info("Push subscription removed for user: {}", userId);
    }
    
    @Transactional
    public void removeAllSubscriptions(String userId) {
        pushSubscriptionRepository.deleteByUserId(userId);
        log.info("All push subscriptions removed for user: {}", userId);
    }
    
    // ==========================
    // STATISTICS
    // ==========================
    public Map<String, Object> getNotificationStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalSent", notificationRepository.countSent());
        stats.put("totalFailed", notificationRepository.countFailed());
        stats.put("totalUnread", notificationRepository.countAllUnread());
        stats.put("totalNotifications", notificationRepository.count());
        
        List<Object[]> typeStats = notificationRepository.countByTypeGroup();
        Map<String, Long> typeMap = new HashMap<>();
        for (Object[] obj : typeStats) {
            typeMap.put((String) obj[0], (Long) obj[1]);
        }
        stats.put("typeStats", typeMap);
        
        return stats;
    }
    
    // ==========================
    // HELPER METHODS
    // ==========================
    private boolean shouldSendEmail(String userId) {
        NotificationPreference pref = getPreferences(userId);
        return pref.isEmailEnabled();
    }
    
    private boolean shouldSendSms(String userId) {
        NotificationPreference pref = getPreferences(userId);
        return pref.isSmsEnabled();
    }
    
    private boolean shouldSendPush(String userId) {
        NotificationPreference pref = getPreferences(userId);
        return pref.isPushEnabled();
    }
}