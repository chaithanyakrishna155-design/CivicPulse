package com.civicpulse.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.civicpulse.entity.Complaint;
import com.civicpulse.repository.ComplaintRepository;
import com.civicpulse.service.ComplaintService;

import java.util.List;
import java.util.stream.Collectors;


@Controller
@RequestMapping("/admin")
public class AssignmentController {


    @Autowired
    private ComplaintRepository complaintRepository;


    @Autowired
    private ComplaintService complaintService;



    // ==========================
    // Check Admin Session
    // ==========================

    private boolean isAdmin(HttpSession session) {

        String role = (String) session.getAttribute("role");

        return "ADMIN".equals(role);
    }




    // ==========================
    // Open Assign Complaint Page
    // ==========================

    @GetMapping("/assign-complaint")
    public String assignPage(
            HttpSession session,
            Model model) {


        if (!isAdmin(session)) {
            return "redirect:/login";
        }


        List<Complaint> complaints =
                complaintRepository.findAll();



        List<Complaint> availableComplaints =
                complaints.stream()

                .filter(c -> {

                    String status = c.getStatus();


                    return status != null
                            &&
                            !"Resolved".equalsIgnoreCase(status)
                            &&
                            !"Assigned".equalsIgnoreCase(status);

                })

                .collect(Collectors.toList());



        model.addAttribute(
                "complaints",
                availableComplaints
        );


        model.addAttribute(
                "totalComplaints",
                availableComplaints.size()
        );



        return "assign-complaint";
    }






    // ==========================
    // Assign Complaint To Officer
    // ==========================

    @PostMapping("/assign-complaint")
    public String assignComplaint(

            @RequestParam Long complaintId,

            @RequestParam String officerEmail,

            HttpSession session,

            RedirectAttributes redirectAttributes) {



        if (!isAdmin(session)) {
            return "redirect:/login";
        }



        try {


            Complaint complaint =
                    complaintRepository
                    .findById(complaintId)
                    .orElse(null);



            if (complaint == null) {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Complaint not found!"
                );


                return "redirect:/admin/assign-complaint";
            }




            String status = complaint.getStatus();



            if ("Resolved".equalsIgnoreCase(status)) {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ This complaint is already resolved!"
                );


                return "redirect:/admin/assign-complaint";
            }





            if ("Assigned".equalsIgnoreCase(status)) {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ This complaint is already assigned!"
                );


                return "redirect:/admin/assign-complaint";
            }





            complaintService.assignOfficer(
                    complaintId,
                    officerEmail
            );



            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Complaint #" 
                    + complaintId 
                    + " assigned to "
                    + officerEmail
                    + " successfully!"
            );



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Assignment failed: "
                    + e.getMessage()
            );

        }



        return "redirect:/admin/assign-complaint";

    }







    // ==========================
    // Get Available Complaints AJAX
    // ==========================

    @GetMapping("/assign-complaint/available")
    @ResponseBody
    public List<Complaint> getAvailableComplaints() {


        return complaintRepository
                .findAll()
                .stream()

                .filter(c -> {

                    String status = c.getStatus();


                    return status != null
                            &&
                            !"Resolved"
                            .equalsIgnoreCase(status)
                            &&
                            !"Assigned"
                            .equalsIgnoreCase(status);

                })

                .collect(Collectors.toList());

    }







    // ==========================
    // View Assigned Complaints
    // ==========================

    @GetMapping("/assigned-complaints")
    public String viewAssignedComplaints(

            HttpSession session,

            Model model) {



        if (!isAdmin(session)) {

            return "redirect:/login";

        }



        List<Complaint> assignedComplaints =
                complaintRepository
                .findByStatus("Assigned");



        model.addAttribute(
                "complaints",
                assignedComplaints
        );



        model.addAttribute(
                "totalAssigned",
                assignedComplaints.size()
        );



        return "admin-assigned-complaints";

    }

}