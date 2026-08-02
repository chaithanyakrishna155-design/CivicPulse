package com.civicpulse.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.civicpulse.entity.OfficerRequest;
import com.civicpulse.entity.User;
import com.civicpulse.repository.OfficerRequestRepository;
import com.civicpulse.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;


@Slf4j
@Service
public class OfficerRequestService {


    @Autowired
    private OfficerRequestRepository officerRequestRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private NotificationService notificationService;




    // =====================================================
    // ADD OFFICER REQUEST
    // =====================================================


    @Transactional
    public OfficerRequest addOfficerEmail(String email) {


        try {


            Optional<OfficerRequest> existingRequest =
                    officerRequestRepository.findByEmail(email);



            if(existingRequest.isPresent()) {


                OfficerRequest request =
                        existingRequest.get();



                if("PENDING".equalsIgnoreCase(request.getStatus())
                        ||
                   "APPROVED".equalsIgnoreCase(request.getStatus())) {


                    log.warn(
                        "Officer request already exists : {}",
                        email
                    );


                    return request;

                }

            }





            Optional<User> existingUser =
                    userRepository.findByEmail(email);



            if(existingUser.isPresent()) {


                User user =
                        existingUser.get();



                if("OFFICER".equalsIgnoreCase(user.getRole())
                        &&
                   user.isApproved()) {


                    log.warn(
                        "Already approved officer : {}",
                        email
                    );


                    return null;

                }

            }





            OfficerRequest request =
                    new OfficerRequest(email);



            request.setStatus("PENDING");

            request.setApproved(false);

            request.setRequestedAt(
                    LocalDateTime.now()
            );



            OfficerRequest saved =
                    officerRequestRepository.save(request);



            notifyAdminAboutNewRequest(saved);



            log.info(
                "Officer request created : {}",
                email
            );



            return saved;



        } catch(Exception e) {


            log.error(
                "Failed creating officer request : {}",
                e.getMessage()
            );


            return null;

        }

    }





    // =====================================================
    // CHECK APPROVAL STATUS
    // =====================================================


    public boolean isOfficerApproved(String email) {


        return officerRequestRepository
                .findByEmail(email)
                .map(OfficerRequest::isApprovedStatus)
                .orElse(false);

    }





    public boolean isOfficerPending(String email) {


        return officerRequestRepository
                .findByEmail(email)
                .map(request ->
                    "PENDING"
                    .equalsIgnoreCase(request.getStatus())
                )
                .orElse(false);

    }





    // =====================================================
    // APPROVE OFFICER BY ID
    // =====================================================


    @Transactional
    public boolean approveOfficer(Long id) {


        OfficerRequest request =
                officerRequestRepository
                .findById(id)
                .orElse(null);



        if(request == null) {


            log.warn(
                "Officer request not found : {}",
                id
            );


            return false;

        }




        return approveRequest(
                request,
                "ADMIN"
        );

    }





    // =====================================================
    // APPROVE OFFICER BY EMAIL
    // =====================================================


    @Transactional
    public boolean approveOfficerByEmail(
            String email,
            String approvedBy) {


        OfficerRequest request =
                officerRequestRepository
                .findByEmail(email)
                .orElse(null);



        if(request == null) {


            log.warn(
                "No request found : {}",
                email
            );


            return false;

        }



        return approveRequest(
                request,
                approvedBy
        );

    }





    // =====================================================
    // COMMON APPROVAL LOGIC
    // =====================================================


    private boolean approveRequest(
            OfficerRequest request,
            String approvedBy) {


        try {


            if(request.isApprovedStatus()) {


                log.warn(
                    "Already approved : {}",
                    request.getEmail()
                );


                return false;

            }



            request.setStatus("APPROVED");

            request.setApproved(true);

            request.setApprovedAt(
                    LocalDateTime.now()
            );

            request.setApprovedBy(
                    approvedBy
            );



            officerRequestRepository.save(request);





            Optional<User> user =
                    userRepository
                    .findByEmail(
                        request.getEmail()
                    );



            if(user.isPresent()) {


                User officer =
                        user.get();



                officer.setRole(
                    "OFFICER"
                );


                officer.setApproved(
                    true
                );


                officer.setUpdatedAt(
                    LocalDateTime.now()
                );


                userRepository.save(
                        officer
                );

            }





            notifyUserAboutApproval(
                    request.getEmail()
            );



            log.info(
                "Officer approved : {}",
                request.getEmail()
            );


            return true;



        } catch(Exception e) {


            log.error(
                "Approval failed : {}",
                e.getMessage()
            );


            return false;

        }

    }
    // =====================================================
    // REJECT OFFICER REQUEST
    // =====================================================

    @Transactional
    public boolean rejectOfficer(Long id, String rejectionReason) {

        try {

            OfficerRequest request =
                    officerRequestRepository
                    .findById(id)
                    .orElse(null);


            if(request == null) {

                log.warn(
                    "Officer request not found : {}",
                    id
                );

                return false;
            }



            request.setStatus("REJECTED");

            request.setApproved(false);

            request.setRejectedAt(
                    LocalDateTime.now()
            );

            request.setRejectionReason(
                    rejectionReason
            );


            officerRequestRepository.save(request);



            log.info(
                "Officer request rejected : {}",
                request.getEmail()
            );


            return true;


        } catch(Exception e) {

            log.error(
                "Reject failed : {}",
                e.getMessage()
            );

            return false;
        }

    }





    // =====================================================
    // DELETE REQUEST
    // =====================================================

    @Transactional
    public boolean deleteOfficerRequest(Long id) {


        try {


            if(officerRequestRepository.existsById(id)) {


                officerRequestRepository.deleteById(id);


                log.info(
                    "Officer request deleted : {}",
                    id
                );


                return true;
            }


            return false;


        } catch(Exception e) {


            log.error(
                "Delete failed : {}",
                e.getMessage()
            );


            return false;

        }

    }





    // =====================================================
    // GET ALL REQUESTS
    // =====================================================


    public List<OfficerRequest> getAllOfficerRequests() {


        return officerRequestRepository.findAll();

    }





    public List<OfficerRequest> getPendingRequests() {


        return officerRequestRepository
                .findPendingRequests();

    }





    public List<OfficerRequest> getApprovedRequests() {


        return officerRequestRepository
                .findByApprovedTrue();

    }





    public List<OfficerRequest> getRejectedRequests() {


        return officerRequestRepository
                .findByStatus("REJECTED");

    }





    // =====================================================
    // GET REQUEST BY EMAIL
    // =====================================================


    public OfficerRequest getRequestByEmail(String email) {


        return officerRequestRepository
                .findByEmail(email)
                .orElse(null);

    }





    // =====================================================
    // STATISTICS
    // =====================================================


    public long getPendingCount() {


        return officerRequestRepository
                .countPending();

    }



    public long getApprovedCount() {


        return officerRequestRepository
                .countApproved();

    }



    public long getRejectedCount() {


        return officerRequestRepository
                .countRejected();

    }



    public long getTotalRequests() {


        return officerRequestRepository.count();

    }





    // =====================================================
    // GET STATUS
    // =====================================================


    public String getStatus(String email) {


        return officerRequestRepository
                .findByEmail(email)
                .map(OfficerRequest::getStatus)
                .orElse("NOT_FOUND");

    }





    // =====================================================
    // NOTIFY ADMIN
    // =====================================================


    private void notifyAdminAboutNewRequest(
            OfficerRequest request) {


        try {


            List<User> admins =
                    userRepository
                    .findByRole("ADMIN");



            for(User admin : admins) {



                java.util.Map<String,Object> data =
                        new java.util.HashMap<>();


                data.put(
                    "title",
                    "New Officer Request"
                );


                data.put(
                    "message",
                    "New officer request from "
                    + request.getEmail()
                );


                data.put(
                    "email",
                    request.getEmail()
                );


                data.put(
                    "requestedAt",
                    request.getRequestedAt()
                    .toString()
                );


                data.put(
                    "link",
                    "/admin/officers"
                );



                notificationService
                .sendEmailNotification(
                    admin.getId().toString(),
                    admin.getEmail(),
                    "New Officer Request",
                    "email/officer-request",
                    data
                );

            }



        } catch(Exception e) {


            log.error(
                "Admin notification failed : {}",
                e.getMessage()
            );

        }

    }





    // =====================================================
    // NOTIFY APPROVAL
    // =====================================================


    private void notifyUserAboutApproval(
            String email) {


        try {


            User user =
                userRepository
                .findByEmail(email)
                .orElse(null);



            if(user == null)
                return;



            java.util.Map<String,Object> data =
                    new java.util.HashMap<>();


            data.put(
                "title",
                "Officer Access Approved"
            );


            data.put(
                "message",
                "Your officer request has been approved"
            );


            data.put(
                "link",
                "/officer/dashboard"
            );



            notificationService
            .sendEmailNotification(
                user.getId().toString(),
                user.getEmail(),
                "Officer Access Approved",
                "email/officer-approval",
                data
            );



            if(user.getPhone()!=null &&
               !user.getPhone().isEmpty()) {


                notificationService
                .sendSmsNotification(
                    user.getId().toString(),
                    user.getPhone(),
                    "CivicPulse: Officer access approved"
                );

            }



        } catch(Exception e) {


            log.error(
                "Approval notification failed : {}",
                e.getMessage()
            );

        }

    }





    // =====================================================
    // CLEAN OLD REQUESTS
    // =====================================================


    @Transactional
    public void cleanupOldRequests() {


        try {


            LocalDateTime limit =
                    LocalDateTime.now()
                    .minusDays(30);



            List<OfficerRequest> rejected =
                    officerRequestRepository
                    .findByStatus("REJECTED");



            for(OfficerRequest request : rejected) {


                if(request.getRejectedAt()!=null &&
                   request.getRejectedAt()
                   .isBefore(limit)) {


                    officerRequestRepository
                    .delete(request);


                    log.info(
                        "Old request deleted : {}",
                        request.getEmail()
                    );

                }

            }



        } catch(Exception e) {


            log.error(
                "Cleanup failed : {}",
                e.getMessage()
            );

        }

    }

}