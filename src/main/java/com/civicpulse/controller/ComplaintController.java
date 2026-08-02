package com.civicpulse.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.civicpulse.entity.Complaint;
import com.civicpulse.entity.User;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.UserService;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


@Controller
public class ComplaintController {


    private static final String UPLOAD_DIR =
            "src/main/resources/static/uploads/";



    @Autowired
    private ComplaintService complaintService;



    @Autowired
    private UserService userService;





    // ==========================
    // Authentication Check
    // ==========================

    private boolean isAuthenticated(HttpSession session) {

        return session.getAttribute("user") != null;

    }





    private boolean isAdmin(HttpSession session) {


        String role =
                (String) session.getAttribute("role");


        return "ADMIN".equals(role);

    }







    // ==========================
    // Register Complaint Page
    // ==========================

    @GetMapping("/complaint/register")
    public String showComplaintForm(
            HttpSession session,
            Model model) {



        if(!isAuthenticated(session)) {

            return "redirect:/login";

        }



        model.addAttribute(
                "complaint",
                new Complaint()
        );


        return "complaint-register";

    }







 // ==========================
 // Save Complaint
 // ==========================

 @PostMapping("/complaint/save")
 public String saveComplaint(

         @ModelAttribute Complaint complaint,

         @RequestParam(
                 value="media",
                 required=false
         )
         MultipartFile media,

         HttpSession session,

         RedirectAttributes redirectAttributes

 ) throws IOException {


     if(!isAuthenticated(session)) {

         return "redirect:/login";

     }


     try {


         User user =
                 (User) session.getAttribute("user");


         // FIX: Link complaint with logged-in user
         if(user != null) {

             complaint.setUser(user);

         }
         else {

             throw new RuntimeException(
                     "User session expired"
             );

         }





         // ==========================
         // Save Image / Video
         // ==========================

         if(media != null && !media.isEmpty()) {


             Path uploadPath =
                     Paths.get(UPLOAD_DIR);



             if(!Files.exists(uploadPath)) {

                 Files.createDirectories(uploadPath);

             }





             String originalName =
                     media.getOriginalFilename();



             if(originalName == null) {

                 originalName = "camera_file";

             }





             String fileName =
                     System.currentTimeMillis()
                     + "_"
                     + originalName
                     .replaceAll("\\s+","_");





             Path filePath =
                     uploadPath.resolve(fileName);



             Files.copy(
                     media.getInputStream(),
                     filePath
             );





             String contentType =
                     media.getContentType();




             if(contentType != null &&
                     contentType.startsWith("image")) {


                 complaint.setPhotoName(fileName);


             }
             else if(contentType != null &&
                     contentType.startsWith("video")) {


                 complaint.setVideoName(fileName);


             }
             else if(originalName.endsWith(".webm")
                     ||
                     originalName.endsWith(".mp4")) {


                 complaint.setVideoName(fileName);

             }


         }






         // Default status

         if(complaint.getStatus()==null ||
                 complaint.getStatus().isEmpty()) {


             complaint.setStatus("Pending");

         }




         complaintService.saveComplaint(
                 complaint
         );





         redirectAttributes.addFlashAttribute(
                 "success",
                 "✅ Complaint submitted successfully! Complaint ID #"
                 + complaint.getId()
         );



     }
     catch(Exception e) {


         redirectAttributes.addFlashAttribute(
                 "error",
                 "❌ Failed to submit complaint: "
                 + e.getMessage()
         );


     }





     return "redirect:/complaint/register";

 }






            





    // ==========================
    // View Complaints
    // ==========================

    @GetMapping("/complaints")
    public String viewComplaints(

            @RequestParam(defaultValue="0")
            int page,

            HttpSession session,

            Model model) {



        if(!isAuthenticated(session)) {

            return "redirect:/login";

        }



        var complaintPage =
                complaintService
                .getAllComplaints(
                        PageRequest.of(page,5)
                );




        model.addAttribute(
                "complaints",
                complaintPage.getContent()
        );

        model.addAttribute(
                "currentPage",
                page
        );


        model.addAttribute(
                "totalPages",
                complaintPage.getTotalPages()
        );


        model.addAttribute(
                "isAdmin",
                isAdmin(session)
        );


        model.addAttribute(
                "isOfficer",
                "OFFICER"
                .equals(session.getAttribute("role"))
        );



        return "admin-complaints";

    }







    // ==========================
    // Search
    // ==========================

    @GetMapping("/complaints/search")
    public String searchComplaints(

            @RequestParam(required=false)
            String keyword,


            @RequestParam(defaultValue="0")
            int page,


            HttpSession session,


            Model model) {



        if(!isAuthenticated(session)) {

            return "redirect:/login";

        }



        var complaintPage =
                complaintService.searchComplaints(
                        keyword,
                        PageRequest.of(page,5)
                );



        model.addAttribute(
                "complaints",
                complaintPage.getContent()
        );


        model.addAttribute(
                "currentPage",
                page
        );


        model.addAttribute(
                "totalPages",
                complaintPage.getTotalPages()
        );


        model.addAttribute(
                "keyword",
                keyword
        );


        return "admin-complaints";

    }







    // ==========================
    // Filter
    // ==========================

    @GetMapping("/complaints/filter")
    public String filterComplaints(

            @RequestParam String status,

            @RequestParam(defaultValue="0")
            int page,

            HttpSession session,

            Model model) {



        if(!isAuthenticated(session)) {

            return "redirect:/login";

        }



        var complaintPage =
                complaintService.filterComplaints(
                        status,
                        PageRequest.of(page,5)
                );



        model.addAttribute(
                "complaints",
                complaintPage.getContent()
        );


        model.addAttribute(
                "filterStatus",
                status
        );


        return "admin-complaints";

    }







    // ==========================
    // My Complaints
    // ==========================

    @GetMapping("/my-complaints")
    public String myComplaints(
            HttpSession session,
            Model model) {



        if(!isAuthenticated(session)) {

            return "redirect:/login";

        }



        User user =
                (User) session.getAttribute("user");



        if(user == null) {

            return "redirect:/login";

        }




        String role =
                (String) session.getAttribute("role");



        List<Complaint> complaints;



        if("OFFICER".equals(role)) {


            complaints =
                    complaintService
                    .getComplaintsByOfficer(
                            user.getEmail()
                    );


        }
        else {


            complaints =
                    complaintService
                    .getComplaintsByUser(
                            user.getId()
                    );


        }





        model.addAttribute(
                "complaints",
                complaints
        );



        model.addAttribute(
                "isOfficer",
                "OFFICER".equals(role)
        );



        return "my-complaints";

    }

}