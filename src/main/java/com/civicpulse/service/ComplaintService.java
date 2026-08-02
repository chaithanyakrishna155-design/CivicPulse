package com.civicpulse.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.repository.ComplaintAssignmentRepository;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.UserRepository;
import com.civicpulse.entity.ComplaintAssignment;

@Service
public class ComplaintService {


    private static final Logger log =
            LoggerFactory.getLogger(ComplaintService.class);


    @Autowired
    private ComplaintRepository complaintRepository;


    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;


    @Autowired
    private UserRepository userRepository;


    @Autowired
    private NotificationService notificationService;



    @Transactional
    public Complaint saveComplaint(Complaint complaint){

        complaint.setStatus("Pending");

        if(complaint.getPriority()==null)
            complaint.setPriority("MEDIUM");

        complaint.setUpdatedAt(LocalDateTime.now());

        Complaint saved =
                complaintRepository.save(complaint);

        notifyAdminAboutNewComplaint(saved);

        log.info("Complaint created {}",saved.getId());

        return saved;
    }



    public List<Complaint> getAllComplaints(){

        return complaintRepository.findAll();
    }


    public Page<Complaint> getAllComplaints(Pageable pageable){

        return complaintRepository.findAll(pageable);
    }



    public Complaint getComplaintById(Long id){

        return complaintRepository
                .findById(id)
                .orElse(null);
    }



    @Transactional
    public void updateComplaint(Complaint complaint){

        Complaint old =
                getComplaintById(complaint.getId());


        if(old==null)
            return;


        String oldStatus=old.getStatus();

        complaint.setUpdatedAt(LocalDateTime.now());

        complaintRepository.save(complaint);


        if(!oldStatus.equals(complaint.getStatus())){

            sendStatusUpdateNotification(
                    complaint,
                    oldStatus
            );
        }

        log.info("Complaint updated {}",complaint.getId());
    }



    @Transactional
    public void deleteComplaint(Long id){

        Complaint complaint =
                getComplaintById(id);


        if(complaint==null)
            return;


        assignmentRepository
                .deleteByComplaintId(id);


        complaintRepository.deleteById(id);


        notifyUserAboutDeletion(complaint);

        log.info("Complaint deleted {}",id);
    }



    public Page<Complaint> searchComplaints(
            String keyword,
            Pageable pageable){


        if(keyword==null || keyword.isBlank())
            return complaintRepository.findAll(pageable);


        return complaintRepository
                .searchComplaints(
                        keyword.trim(),
                        pageable
                );
    }
 // =====================================================
 // FILTER
 // =====================================================

 public Page<Complaint> filterComplaints(
         String status,
         Pageable pageable){


     if(status==null || status.equalsIgnoreCase("All")){

         return complaintRepository.findAll(pageable);
     }


     return complaintRepository
             .findByStatus(
                     status,
                     pageable
             );
 }




 public Page<Complaint> filterComplaints(
         String status,
         String category,
         String priority,
         String location,
         Pageable pageable){


     return complaintRepository
             .filterComplaints(
                     status,
                     category,
                     location,
                     pageable
             );

 }



 // =====================================================
 // DASHBOARD COUNTS
 // =====================================================


 public long getTotalComplaints(){

     return complaintRepository.count();
 }



 public long getPendingComplaints(){

     return complaintRepository
             .getPendingCount();
 }



 public long getAssignedComplaints(){

     return complaintRepository
             .getAssignedCount();
 }



 public long getInProgressComplaints(){

     return complaintRepository
             .getInProgressCount();
 }



 public long getResolvedComplaints(){

     return complaintRepository
             .getResolvedCount();
 }



 // =====================================================
 // ASSIGN OFFICER
 // =====================================================


 @Transactional
 public boolean assignOfficer(
         Long complaintId,
         String officerEmail){


     Complaint complaint =
             getComplaintById(complaintId);


     if(complaint==null)
         return false;



     User officer =
             userRepository
             .findByEmail(officerEmail)
             .orElse(null);



     if(officer==null)
         return false;



     complaint.setAssignedOfficer(
             officerEmail
     );


     complaint.setStatus(
             "Assigned"
     );


     complaint.setUpdatedAt(
             LocalDateTime.now()
     );


     complaintRepository.save(
             complaint
     );
     ComplaintAssignment assignment =
    	        new ComplaintAssignment();


    	assignment.setComplaint(
    	        complaint
    	);


    	assignment.setOfficer(
    	        officer
    	);


    	assignment.setStatus(
    	        "ASSIGNED"
    	);


    	assignment.setAssignedAt(
    	        LocalDateTime.now()
    	);


    	assignmentRepository.save(
    	        assignment
    	);



     notifyOfficerAboutAssignment(
             complaint,
             officerEmail
     );


     notifyUserAboutAssignment(
             complaint
     );



     log.info(
         "Complaint {} assigned to {}",
         complaintId,
         officerEmail
     );


     return true;
 }




 // =====================================================
 // UPDATE STATUS
 // =====================================================


 @Transactional
 public void updateComplaintStatus(
         Long complaintId,
         String newStatus){


     Complaint complaint =
             getComplaintById(complaintId);



     if(complaint==null)
         return;



     String oldStatus =
             complaint.getStatus();



     complaint.setStatus(
             newStatus
     );


     complaint.setUpdatedAt(
             LocalDateTime.now()
     );


     complaintRepository.save(
             complaint
     );



     sendStatusUpdateNotification(
             complaint,
             oldStatus
     );



     log.info(
         "Status changed {} -> {}",
         oldStatus,
         newStatus
     );
 }




 // =====================================================
 // UPDATE PRIORITY
 // =====================================================


 @Transactional
 public void updatePriority(
         Long complaintId,
         String priority){


     Complaint complaint =
             getComplaintById(complaintId);



     if(complaint==null)
         return;



     complaint.setPriority(priority);


     complaint.setUpdatedAt(
             LocalDateTime.now()
     );


     complaintRepository.save(
             complaint
     );


 }




//=====================================================
//MARK URGENT
//=====================================================

@Transactional
public void markAsUrgent(
      Long complaintId,
      boolean urgent){


  Complaint complaint =
          getComplaintById(complaintId);


  if(complaint == null)
      return;


  complaint.setUrgent(urgent);


  complaint.setUpdatedAt(
          LocalDateTime.now()
  );


  complaintRepository.save(
          complaint
  );

}
  // =====================================================
  // GET COMPLAINTS BY USER
  // =====================================================


  public List<Complaint> getComplaintsByUser(Long userId){

      return complaintRepository
              .findByUser_Id(userId);

  }



  public Page<Complaint> getComplaintsByUser(
          Long userId,
          Pageable pageable){

      return complaintRepository
              .findByUser_Id(
                      userId,
                      pageable
              );

  }



  // =====================================================
  // GET COMPLAINTS BY OFFICER
  // =====================================================


  public List<Complaint> getComplaintsByOfficer(
          String officerEmail){


      return complaintRepository
              .findByAssignedOfficer(
                      officerEmail
              );

  }




  // =====================================================
  // CATEGORY
  // =====================================================


  public List<Complaint> getComplaintsByCategory(
          String category){


      return complaintRepository
              .findByCategory(
                      category
              );

  }




  public List<String> getCategories(){


      return complaintRepository
              .findDistinctCategories();

  }



  // =====================================================
  // RESOLUTION NOTES
  // =====================================================


  @Transactional
  public void addResolutionNotes(
          Long complaintId,
          String notes){


      Complaint complaint =
              getComplaintById(complaintId);



      if(complaint==null)
          return;



      complaint.setResolutionNotes(
              notes
      );


      complaint.setUpdatedAt(
              LocalDateTime.now()
      );


      complaintRepository.save(
              complaint
      );

  }




  // =====================================================
  // EVIDENCE
  // =====================================================


  public List<Complaint> getComplaintsWithEvidence(){


      return complaintRepository
              .findComplaintsWithEvidence();

  }



  public long getComplaintsWithEvidenceCount(){


      return complaintRepository
              .countComplaintsWithEvidence();

  }




  // =====================================================
  // STATISTICS
  // =====================================================


  public Map<String,Object> getComplaintStatistics(){


      Map<String,Object> stats =
              new HashMap<>();



      stats.put(
              "total",
              getTotalComplaints()
      );


      stats.put(
              "pending",
              getPendingComplaints()
      );


      stats.put(
              "assigned",
              getAssignedComplaints()
      );


      stats.put(
              "inProgress",
              getInProgressComplaints()
      );


      stats.put(
              "resolved",
              getResolvedComplaints()
      );



      stats.put(
              "statusDistribution",
              complaintRepository
                      .countByStatusGroup()
      );



      stats.put(
              "evidenceCount",
              getComplaintsWithEvidenceCount()
      );


      return stats;
  }
//=====================================================
//NOTIFY ADMIN ABOUT NEW COMPLAINT
//=====================================================

private void notifyAdminAboutNewComplaint(
       Complaint complaint){


   try{


       List<User> admins =
               userRepository
               .findByRole("ADMIN");



       for(User admin : admins){


           Map<String,Object> data =
                   new HashMap<>();


           data.put(
                   "title",
                   "New Complaint Registered"
           );


           data.put(
                   "message",
                   "New complaint #" 
                   + complaint.getId()
           );


           data.put(
                   "status",
                   complaint.getStatus()
           );



           notificationService.sendEmailNotification(
                   admin.getId().toString(),
                   admin.getEmail(),
                   "New Complaint",
                   "email/complaint-status",
                   data
           );

       }


   }catch(Exception e){

       log.error(
               "Admin notification failed {}",
               e.getMessage()
       );

   }

}



//=====================================================
//STATUS UPDATE NOTIFICATION
//=====================================================


private void sendStatusUpdateNotification(
       Complaint complaint,
       String oldStatus){


   try{


       User user =
               userRepository
               .findById(
                   complaint.getUserId()
               )
               .orElse(null);



       if(user==null)
           return;



       Map<String,Object> data =
               new HashMap<>();


       data.put(
               "title",
               "Complaint Status Updated"
       );


       data.put(
               "message",
               "Status changed from "
               + oldStatus
               + " to "
               + complaint.getStatus()
       );


       data.put(
               "status",
               complaint.getStatus()
       );



       notificationService.sendEmailNotification(
               user.getId().toString(),
               user.getEmail(),
               "Complaint Status Update",
               "email/complaint-status",
               data
       );


   }catch(Exception e){

       log.error(
               "Status notification failed {}",
               e.getMessage()
       );

   }

}




//=====================================================
//OFFICER NOTIFICATION
//=====================================================


private void notifyOfficerAboutAssignment(
       Complaint complaint,
       String officerEmail){


   try{


       User officer =
               userRepository
               .findByEmail(officerEmail)
               .orElse(null);



       if(officer==null)
           return;



       Map<String,Object> data =
               new HashMap<>();


       data.put(
               "title",
               "Complaint Assigned"
       );


       data.put(
               "message",
               "Complaint #"
               + complaint.getId()
               + " assigned to you"
       );



       notificationService.sendEmailNotification(
               officer.getId().toString(),
               officer.getEmail(),
               "Complaint Assigned",
               "email/complaint-status",
               data
       );



   }catch(Exception e){

       log.error(
               "Officer notification failed {}",
               e.getMessage()
       );

   }

}



//=====================================================
//USER ASSIGNMENT NOTIFICATION
//=====================================================


private void notifyUserAboutAssignment(
       Complaint complaint){


   try{


       User user =
               userRepository
               .findById(
                       complaint.getUserId()
               )
               .orElse(null);



       if(user==null)
           return;



       notificationService.sendEmailNotification(
               user.getId().toString(),
               user.getEmail(),
               "Complaint Assigned",
               "email/complaint-status",
               new HashMap<>()
       );


   }catch(Exception e){

       log.error(
               "User notification failed {}",
               e.getMessage()
       );

   }

}




//=====================================================
//DELETE NOTIFICATION
//=====================================================


private void notifyUserAboutDeletion(
       Complaint complaint){


   try{


       User user =
               userRepository
               .findById(
                       complaint.getUserId()
               )
               .orElse(null);



       if(user==null)
           return;



       notificationService.sendEmailNotification(
               user.getId().toString(),
               user.getEmail(),
               "Complaint Deleted",
               "email/complaint-status",
               new HashMap<>()
       );


   }catch(Exception e){

       log.error(
               "Delete notification failed {}",
               e.getMessage()
       );

   }

}



//=====================================================
//BULK OPERATIONS
//=====================================================


@Transactional
public void bulkUpdateStatus(
       List<Long> ids,
       String status){


   complaintRepository
           .bulkUpdateStatus(
                   ids,
                   status
           );

}




@Transactional
public void bulkAssign(
       List<Long> ids,
       String officer){


   complaintRepository
           .bulkAssignOfficer(
                   ids,
                   officer
           );

}




@Transactional
public void bulkDelete(
       List<Long> ids){


   complaintRepository
           .deleteAllById(
                   ids
           );

}


}
