package com.civicpulse.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private UserService userService;


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
    // Admin Dashboard
    // ==========================

    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {

        if (!isAdmin(session)) {
            return "redirect:/login";
        }


        model.addAttribute("message", "Welcome Admin");

        model.addAttribute(
                "totalComplaints",
                complaintService.getTotalComplaints()
        );


        model.addAttribute(
                "pendingComplaints",
                complaintService.getPendingComplaints()
        );


        model.addAttribute(
                "inProgressComplaints",
                complaintService.getInProgressComplaints()
        );


        model.addAttribute(
                "resolvedComplaints",
                complaintService.getResolvedComplaints()
        );


        List<User> officers = userService.getAllOfficers();


        model.addAttribute(
                "totalOfficers",
                officers != null ? officers.size() : 0
        );


        return "admin-dashboard";
    }





    // ==========================
    // View All Complaints
    // ==========================

    @GetMapping("/complaints")
    public String viewAllComplaints(HttpSession session, Model model) {


        if (!isAdmin(session)) {
            return "redirect:/login";
        }


        List<Complaint> complaints =
                complaintService.getAllComplaints();


        model.addAttribute(
                "complaints",
                complaints
        );


        return "admin-complaints";
    }






    // ==========================
    // Add Officer Page
    // ==========================

    @GetMapping("/add-officer")
    public String addOfficerPage(HttpSession session) {


        if (!isAdmin(session)) {
            return "redirect:/login";
        }


        return "add-officer";
    }





    // ==========================
    // Give Officer Role
    // ==========================

    @PostMapping("/add-officer")
    public String addOfficer(
            @RequestParam String email,
            HttpSession session,
            RedirectAttributes redirectAttributes) {


        if (!isAdmin(session)) {
            return "redirect:/login";
        }



        try {


            boolean result =
                    userService.makeOfficer(email);



            if(result) {

                redirectAttributes.addFlashAttribute(
                        "success",
                        "✅ Officer access given successfully to " + email
                );

            }
            else {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ User with email '" + email + "' not found!"
                );
            }



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Failed to add officer: " + e.getMessage()
            );

        }



        return "redirect:/admin/add-officer";
    }







    // ==========================
    // Assign Officer Page
    // ==========================

    @GetMapping("/assign/{id}")
    public String assignOfficerPage(
            @PathVariable Long id,
            HttpSession session,
            Model model) {



        if (!isAdmin(session)) {
            return "redirect:/login";
        }



        Complaint complaint =
                complaintService.getComplaintById(id);



        if(complaint == null) {

            return "redirect:/admin/complaints";

        }




        List<User> officers =
                userService.getAllOfficers();



        if(officers == null || officers.isEmpty()) {

            model.addAttribute(
                    "error",
                    "No officers available. Please add officers first."
            );

        }



        model.addAttribute(
                "complaint",
                complaint
        );


        model.addAttribute(
                "officers",
                officers
        );



        return "assign-officer";

    }







    // ==========================
    // Assign Complaint
    // ==========================

    @PostMapping("/assign")
    public String assignOfficer(
            @RequestParam Long complaintId,
            @RequestParam String officerEmail,
            HttpSession session,
            RedirectAttributes redirectAttributes) {



        if (!isAdmin(session)) {

            return "redirect:/login";

        }




        try {


            Complaint complaint =
                    complaintService.getComplaintById(complaintId);



            if(complaint == null) {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Complaint not found!"
                );


                return "redirect:/admin/complaints";

            }




            complaintService.assignOfficer(
                    complaintId,
                    officerEmail
            );




            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Complaint #" + complaintId +
                    " assigned to " + officerEmail
            );



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Assignment failed: " + e.getMessage()
            );

        }




        return "redirect:/admin/complaints";

    }







    // ==========================
    // View Officers
    // ==========================

    @GetMapping("/officers")
    public String viewOfficers(
            HttpSession session,
            Model model) {



        if (!isAdmin(session)) {

            return "redirect:/login";

        }



        List<User> officers =
                userService.getAllOfficers();



        model.addAttribute(
                "officers",
                officers
        );


        model.addAttribute(
                "totalOfficers",
                officers != null ? officers.size() : 0
        );



        return "admin-officers";

    }







    // ==========================
    // Remove Officer
    // ==========================

    @PostMapping("/remove-officer")
    public String removeOfficer(
            @RequestParam Long userId,
            HttpSession session,
            RedirectAttributes redirectAttributes) {



        if (!isAdmin(session)) {

            return "redirect:/login";

        }





        try {


            boolean removed =
                    userService.removeOfficerRole(userId);



            if(removed) {


                redirectAttributes.addFlashAttribute(
                        "success",
                        "✅ Officer role removed successfully!"
                );

            }
            else {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Failed to remove officer role!"
                );

            }



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Error: " + e.getMessage()
            );

        }



        return "redirect:/admin/officers";

    }

}