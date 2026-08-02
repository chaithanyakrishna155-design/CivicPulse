package com.civicpulse.service;


import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.ComplaintAssignment;
import com.civicpulse.entity.User;
import com.civicpulse.repository.ComplaintAssignmentRepository;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;


@Service
@Slf4j
public class ComplaintAssignmentService {


    @Autowired
    private ComplaintAssignmentRepository assignmentRepository;


    @Autowired
    private ComplaintRepository complaintRepository;


    @Autowired
    private UserRepository userRepository;



    // ==========================================
    // ASSIGN COMPLAINT TO OFFICER
    // ==========================================

    @Transactional
    public boolean assignComplaint(
            Complaint complaint,
            String officerEmail) {


        try {


            User officer =
                    userRepository
                    .findByEmail(officerEmail)
                    .orElse(null);



            if(officer == null){

                log.error(
                    "Officer not found : {}",
                    officerEmail
                );

                return false;
            }



            ComplaintAssignment assignment =
                    assignmentRepository
                    .findByComplaint(complaint)
                    .orElse(
                        new ComplaintAssignment()
                    );



            assignment.setComplaint(
                    complaint
            );


            assignment.setOfficer(
                    officer
            );


            assignment.setStatus(
                    "ASSIGNED"
            );


            assignment.setAssignedBy(
                    "ADMIN"
            );


            assignment.setAssignedAt(
                    LocalDateTime.now()
            );



            assignmentRepository.save(
                    assignment
            );



            // update complaint table also

            complaint.setAssignedOfficer(
                    officer.getEmail()
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



            log.info(
              "Complaint {} assigned to {}",
              complaint.getId(),
              officerEmail
            );



            return true;


        }
        catch(Exception e){


            log.error(
                "Assignment Error : {}",
                e.getMessage()
            );


            return false;

        }

    }
    // ==========================================
    // GET OFFICER ASSIGNED COMPLAINTS
    // ==========================================

    @Transactional(readOnly = true)
    public List<ComplaintAssignment> getOfficerComplaints(
            User officer){


        return assignmentRepository
                .findByOfficerOrderByAssignedAtDesc(
                        officer
                );

    }





    // ==========================================
    // GET SINGLE ASSIGNMENT
    // ==========================================

    @Transactional(readOnly = true)
    public ComplaintAssignment getAssignmentById(
            Long id){


        return assignmentRepository
                .findById(id)
                .orElse(null);

    }





    // ==========================================
    // UPDATE ASSIGNMENT STATUS
    // ==========================================

    @Transactional
    public void updateAssignment(
            ComplaintAssignment assignment){



        if(assignment == null)
            return;




        Complaint complaint =
                assignment.getComplaint();




        String status =
                assignment.getStatus();




        // Update complaint status also

        if(complaint != null){



            if("ASSIGNED"
                    .equalsIgnoreCase(status)){


                complaint.setStatus(
                        "Assigned"
                );


            }




            else if("IN PROGRESS"
                    .equalsIgnoreCase(status)){


                complaint.setStatus(
                        "In Progress"
                );


                if(assignment.getStartedAt()==null){

                    assignment.setStartedAt(
                            LocalDateTime.now()
                    );

                }

            }




            else if("RESOLVED"
                    .equalsIgnoreCase(status)){


                complaint.setStatus(
                        "Resolved"
                );


                assignment.setCompleted(
                        true
                );



                if(assignment.getResolvedAt()==null){

                    assignment.setResolvedAt(
                            LocalDateTime.now()
                    );

                }

            }




            complaint.setUpdatedAt(
                    LocalDateTime.now()
            );


            complaintRepository.save(
                    complaint
            );

        }





        assignment.setUpdatedAt(
                LocalDateTime.now()
        );



        assignmentRepository.save(
                assignment
        );


    }





    // ==========================================
    // START WORK
    // ==========================================

    @Transactional
    public boolean startWork(
            Long id){



        ComplaintAssignment assignment =
                getAssignmentById(id);



        if(assignment == null)
            return false;




        if(!"ASSIGNED"
                .equalsIgnoreCase(
                        assignment.getStatus()
                )){


            return false;

        }




        assignment.setStatus(
                "IN PROGRESS"
        );



        assignment.setStartedAt(
                LocalDateTime.now()
        );



        updateAssignment(
                assignment
        );



        return true;


    }
    // ==========================================
    // RESOLVE COMPLAINT
    // ==========================================

    @Transactional
    public boolean resolveComplaint(
            Long id,
            String notes){



        ComplaintAssignment assignment =
                getAssignmentById(id);



        if(assignment == null)
            return false;




        assignment.setStatus(
                "RESOLVED"
        );



        assignment.setResolutionNotes(
                notes
        );



        assignment.setResolvedAt(
                LocalDateTime.now()
        );



        assignment.setCompleted(
                true
        );



        updateAssignment(
                assignment
        );



        return true;


    }





    // ==========================================
    // STATISTICS
    // ==========================================


    @Transactional(readOnly = true)
    public long getTotalAssignments(){


        return assignmentRepository.count();


    }





    @Transactional(readOnly = true)
    public long getAssignedCount(){


        return assignmentRepository
                .countByStatus(
                        "ASSIGNED"
                );


    }





    @Transactional(readOnly = true)
    public long getInProgressCount(){


        return assignmentRepository
                .countByStatus(
                        "IN PROGRESS"
                );


    }





    @Transactional(readOnly = true)
    public long getResolvedCount(){


        return assignmentRepository
                .countByStatus(
                        "RESOLVED"
                );


    }





    // ==========================================
    // FIND ASSIGNMENTS BY OFFICER EMAIL
    // ==========================================


    @Transactional(readOnly = true)
    public List<ComplaintAssignment> 
    getAssignmentsByOfficerEmail(
            String email){



        User officer =
                userRepository
                .findByEmail(email)
                .orElse(null);



        if(officer == null){

            return List.of();

        }




        return assignmentRepository
                .findByOfficerOrderByAssignedAtDesc(
                        officer
                );


    }





    // ==========================================
    // UNASSIGN COMPLAINT
    // ==========================================


    @Transactional
    public boolean unassignComplaint(
            Long complaintId){



        Complaint complaint =
                complaintRepository
                .findById(
                        complaintId
                )
                .orElse(null);



        if(complaint == null){

            return false;

        }




        assignmentRepository
                .deleteByComplaintId(
                        complaintId
                );



        complaint.setAssignedOfficer(
                null
        );



        complaint.setStatus(
                "Pending"
        );



        complaint.setUpdatedAt(
                LocalDateTime.now()
        );



        complaintRepository.save(
                complaint
        );



        return true;


    }





    // ==========================================
    // CHECK OFFICER OWNERSHIP
    // ==========================================


    public boolean isAssignmentOwner(
            Long assignmentId,
            Long officerId){



        ComplaintAssignment assignment =
                getAssignmentById(
                        assignmentId
                );



        if(assignment == null)
            return false;




        return assignment
                .getOfficer()
                .getId()
                .equals(
                        officerId
                );


    }
    // ==========================================
    // GET ASSIGNMENT COUNT BY OFFICER
    // ==========================================

    @Transactional(readOnly = true)
    public long getOfficerAssignmentCount(
            User officer){


        return assignmentRepository
                .findByOfficerOrderByAssignedAtDesc(
                        officer
                )
                .size();

    }





    // ==========================================
    // UPDATE RESOLUTION DETAILS
    // ==========================================

    @Transactional
    public boolean updateResolution(
            Long id,
            String notes){



        ComplaintAssignment assignment =
                getAssignmentById(id);



        if(assignment == null)
            return false;




        assignment.setResolutionNotes(
                notes
        );



        assignment.setResolvedAt(
                LocalDateTime.now()
        );



        assignment.setStatus(
                "RESOLVED"
        );



        assignment.setCompleted(
                true
        );



        updateAssignment(
                assignment
        );



        return true;

    }





    // ==========================================
    // DELETE ALL ASSIGNMENTS OF COMPLAINT
    // ==========================================

    @Transactional
    public void deleteAssignmentsByComplaint(
            Long complaintId){


        assignmentRepository
                .deleteByComplaintId(
                        complaintId
                );

    }





    // ==========================================
    // DEBUG METHOD
    // ==========================================

    @Transactional(readOnly = true)
    public void printOfficerAssignments(
            User officer){


        List<ComplaintAssignment> list =
                assignmentRepository
                .findByOfficerOrderByAssignedAtDesc(
                        officer
                );


        for(ComplaintAssignment a : list){


            log.info(
              "Assignment ID : {} | Complaint ID : {} | Title : {} | Status : {}",
              a.getId(),
              a.getComplaint().getId(),
              a.getComplaint().getTitle(),
              a.getStatus()
            );


        }


    }


}