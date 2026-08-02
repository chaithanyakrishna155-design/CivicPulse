package com.civicpulse.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.civicpulse.entity.ComplaintAssignment;
import com.civicpulse.entity.User;
import com.civicpulse.service.ComplaintAssignmentService;

@Controller
@RequestMapping("/officer")
public class OfficerController {


    @Autowired
    private ComplaintAssignmentService assignmentService;





    // ==========================
    // Authentication Check
    // ==========================

    private boolean isAuthenticated(HttpSession session) {

        return session.getAttribute("user") != null;

    }





    private boolean isOfficer(HttpSession session) {

        String role =
                (String) session.getAttribute("role");

        return "OFFICER".equals(role);

    }





    private User getLoggedInUser(HttpSession session) {

        return (User) session.getAttribute("user");

    }








    // ==========================
    // Officer Dashboard
    // ==========================

    @GetMapping("/dashboard")
    public String officerDashboard(
            HttpSession session,
            Model model) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return "redirect:/login";

        }



        User officer =
                getLoggedInUser(session);



        if(officer != null) {


            List<ComplaintAssignment> assignments =
                    assignmentService
                    .getOfficerComplaints(officer);



            long assignedCount =
                    assignments.stream()

                    .filter(a ->
                            "ASSIGNED"
                            .equalsIgnoreCase(
                                    a.getStatus()
                            ))

                    .count();



            long inProgressCount =
                    assignments.stream()

                    .filter(a ->
                            "IN PROGRESS"
                            .equalsIgnoreCase(
                                    a.getStatus()
                            ))

                    .count();




            long resolvedCount =
                    assignments.stream()

                    .filter(a ->
                            "RESOLVED"
                            .equalsIgnoreCase(
                                    a.getStatus()
                            ))

                    .count();




            model.addAttribute(
                    "assignments",
                    assignments
            );


            model.addAttribute(
                    "assignedCount",
                    assignedCount
            );


            model.addAttribute(
                    "inProgressCount",
                    inProgressCount
            );


            model.addAttribute(
                    "resolvedCount",
                    resolvedCount
            );


            model.addAttribute(
                    "totalAssignments",
                    assignments.size()
            );

        }



        model.addAttribute(
                "officer",
                officer
        );



        model.addAttribute(
                "message",
                "Welcome Officer "
                +
                (officer != null ?
                officer.getFullName()
                :
                "")
        );



        return "officer-dashboard";

    }









    // ==========================
    // Start Complaint Work
    // ==========================

    @GetMapping("/start/{id}")
    public String startWork(

            @PathVariable Long id,

            HttpSession session,

            RedirectAttributes redirectAttributes) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return "redirect:/login";

        }



        try {


            ComplaintAssignment assignment =
                    assignmentService
                    .getAssignmentById(id);



            if(assignment == null) {

                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Assignment not found!"
                );

                return "redirect:/officer/dashboard";

            }




            User officer =
                    getLoggedInUser(session);



            if(!assignment.getOfficer()
                    .getId()
                    .equals(officer.getId())) {


                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Unauthorized access!"
                );


                return "redirect:/officer/dashboard";

            }




            assignment.setStatus(
                    "IN PROGRESS"
            );


            assignmentService
            .updateAssignment(assignment);



            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Work started successfully!"
            );



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    "❌ Failed: "
                    + e.getMessage()
            );

        }



        return "redirect:/officer/dashboard";

    }









    // ==========================
    // Resolve Complaint
    // ==========================

    @GetMapping("/resolve/{id}")
    public String resolveComplaint(

            @PathVariable Long id,

            HttpSession session,

            RedirectAttributes redirectAttributes) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return "redirect:/login";

        }



        try {


            ComplaintAssignment assignment =
                    assignmentService
                    .getAssignmentById(id);



            if(assignment == null) {

                return "redirect:/officer/dashboard";

            }



            User officer =
                    getLoggedInUser(session);



            if(!assignment.getOfficer()
                    .getId()
                    .equals(officer.getId())) {


                return "redirect:/officer/dashboard";

            }





            assignment.setStatus(
                    "RESOLVED"
            );



            assignmentService
            .updateAssignment(assignment);




            assignment.getComplaint()
                    .setStatus("Resolved");



            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Complaint resolved successfully!"
            );



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        }




        return "redirect:/officer/dashboard";

    }









    // ==========================
    // Update Status
    // ==========================

    @PostMapping("/update-status")
    public String updateStatus(

            @RequestParam Long id,

            @RequestParam String status,

            HttpSession session,

            RedirectAttributes redirectAttributes) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return "redirect:/login";

        }



        try {


            ComplaintAssignment assignment =
                    assignmentService
                    .getAssignmentById(id);



            if(assignment == null) {

                return "redirect:/officer/dashboard";

            }




            User officer =
                    getLoggedInUser(session);



            if(!assignment.getOfficer()
                    .getId()
                    .equals(officer.getId())) {

                return "redirect:/officer/dashboard";

            }




            status =
            status.toUpperCase();




            if(!status.equals("ASSIGNED")
                    &&
               !status.equals("IN PROGRESS")
                    &&
               !status.equals("RESOLVED")) {



                redirectAttributes.addFlashAttribute(
                        "error",
                        "❌ Invalid status!"
                );


                return "redirect:/officer/dashboard";

            }




            assignment.setStatus(status);



            assignmentService
            .updateAssignment(assignment);




            if(status.equals("RESOLVED")) {

                assignment.getComplaint()
                .setStatus("Resolved");

            }




            redirectAttributes.addFlashAttribute(
                    "success",
                    "✅ Status updated successfully!"
            );



        }
        catch(Exception e) {


            redirectAttributes.addFlashAttribute(
                    "error",
                    e.getMessage()
            );

        }




        return "redirect:/officer/dashboard";

    }









    // ==========================
    // Assignment Details
    // ==========================

    @GetMapping("/assignment/{id}")
    public String viewAssignment(

            @PathVariable Long id,

            HttpSession session,

            Model model) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return "redirect:/login";

        }



        ComplaintAssignment assignment =
                assignmentService
                .getAssignmentById(id);



        if(assignment == null) {

            return "redirect:/officer/dashboard";

        }




        User officer =
                getLoggedInUser(session);



        if(!assignment.getOfficer()
                .getId()
                .equals(officer.getId())) {

            return "redirect:/officer/dashboard";

        }




        model.addAttribute(
                "assignment",
                assignment
        );

        model.addAttribute(
                "officer",
                officer
        );



        return "officer-assignment-detail";

    }









    // ==========================
    // JSON API
    // ==========================

    @GetMapping("/api/assignments")
    @ResponseBody
    public List<ComplaintAssignment> getAssignments(
            HttpSession session) {



        if(!isAuthenticated(session)
                ||
                !isOfficer(session)) {

            return List.of();

        }



        User officer =
                getLoggedInUser(session);



        if(officer == null) {

            return List.of();

        }



        return assignmentService
                .getOfficerComplaints(officer);

    }

}