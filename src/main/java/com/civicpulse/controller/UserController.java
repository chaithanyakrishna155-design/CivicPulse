package com.civicpulse.controller;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.civicpulse.config.AdminConfig;
import com.civicpulse.entity.User;
import com.civicpulse.service.ComplaintService;
import com.civicpulse.service.OfficerRequestService;
import com.civicpulse.service.UserService;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private OfficerRequestService officerRequestService;

    // ==========================
    // Check Authentication
    // ==========================
    private boolean isAuthenticated(HttpSession session) {
        return session.getAttribute("user") != null;
    }

    private User getLoggedInUser(HttpSession session) {
        return (User) session.getAttribute("user");
    }

    // ==========================
    // Registration Page
    // ==========================
    @GetMapping("/register")
    public String showRegisterPage(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    // ==========================
    // Register User
    // ==========================
    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        try {
            if (userService.emailExists(user.getEmail())) {
                model.addAttribute("error", "❌ Email already exists!");
                return "register";
            }

            // Officer Approval Check
            if (officerRequestService.isOfficerApproved(user.getEmail())) {
                user.setRole("OFFICER");
                redirectAttributes.addFlashAttribute("success", "✅ Registered as Officer!");
            } else {
                user.setRole("CITIZEN");
                redirectAttributes.addFlashAttribute("success", "✅ Registration successful! Please login.");
            }

            userService.registerUser(user);
            return "redirect:/login";

        } catch (Exception e) {
            model.addAttribute("error", "❌ Registration failed: " + e.getMessage());
            return "register";
        }
    }

    // ==========================
    // Login Page
    // ==========================
    @GetMapping("/login")
    public String showLoginPage(HttpSession session) {
        // If already logged in, redirect to appropriate dashboard
        if (isAuthenticated(session)) {
            User user = getLoggedInUser(session);
            if ("ADMIN".equals(user.getRole())) {
                return "redirect:/admin/dashboard";
            } else if ("OFFICER".equals(user.getRole())) {
                return "redirect:/officer/dashboard";
            } else {
                return "redirect:/dashboard";
            }
        }
        return "login";
    }

 // ==========================
 // Login
 // ==========================
 @PostMapping("/login")
 public String loginUser(
         @RequestParam String email,
         @RequestParam String password,
         HttpSession session,
         Model model,
         RedirectAttributes redirectAttributes) {

     try {

         System.out.println("========== LOGIN DEBUG ==========");
         System.out.println("Email received : " + email);
         System.out.println("Password received : " + password);


         // -------- ADMIN LOGIN --------
         if (email.equalsIgnoreCase(AdminConfig.ADMIN_EMAIL)
                 && password.equals(AdminConfig.ADMIN_PASSWORD)) {


             User adminUser = new User();

             adminUser.setEmail(email);
             adminUser.setFullName("Admin");
             adminUser.setRole("ADMIN");


             session.setAttribute("user", adminUser);
             session.setAttribute("role", "ADMIN");
             session.setAttribute("email", email);


             redirectAttributes.addFlashAttribute(
                     "success",
                     "✅ Welcome Admin!"
             );


             return "redirect:/admin/dashboard";
         }



         // -------- NORMAL USER LOGIN --------

         User user = userService.loginUser(email, password);



         System.out.println("User returned : " + user);



         if(user == null){

             System.out.println("LOGIN FAILED");

             model.addAttribute(
                     "error",
                     "❌ Invalid Email or Password"
             );

             return "login";
         }



         System.out.println(
                 "LOGIN SUCCESS : "
                 + user.getEmail()
         );



         session.setAttribute(
                 "user",
                 user
         );


         session.setAttribute(
                 "role",
                 user.getRole()
         );


         session.setAttribute(
                 "email",
                 user.getEmail()
         );



         // Officer
         if("OFFICER".equalsIgnoreCase(user.getRole())){


             redirectAttributes.addFlashAttribute(
                     "success",
                     "✅ Welcome Officer "
                     + user.getFullName()
             );


             return "redirect:/officer/dashboard";
         }



         // Citizen

         redirectAttributes.addFlashAttribute(
                 "success",
                 "✅ Welcome "
                 + user.getFullName()
         );


         return "redirect:/dashboard";



     }
     catch(Exception e){


         e.printStackTrace();


         model.addAttribute(
                 "error",
                 "❌ Login failed : "
                 + e.getMessage()
         );


         return "login";
     }

 }
    // ==========================
    // Logout
    // ==========================
    @GetMapping("/logout")
    public String logout(HttpSession session, RedirectAttributes redirectAttributes) {
        session.invalidate();
        redirectAttributes.addFlashAttribute("success", "✅ Logged out successfully!");
        return "redirect:/login";
    }

    // ==========================
    // Citizen Dashboard
    // ==========================
    @GetMapping("/citizen/dashboard")
    public String citizenDashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (user == null || !"CITIZEN".equals(user.getRole())) {
            return "redirect:/login";
        }

        addDashboardStats(model);
        model.addAttribute("user", user);
        return "dashboard";
    }

    // ==========================
    // Dashboard (Generic - redirects based on role)
    // ==========================
    @GetMapping("/dashboard")
    public String showDashboard(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        // Redirect based on role
        if ("ADMIN".equals(user.getRole())) {
            return "redirect:/admin/dashboard";
        } else if ("OFFICER".equals(user.getRole())) {
            return "redirect:/officer/dashboard";
        }

        addDashboardStats(model);
        model.addAttribute("user", user);
        return "dashboard";
    }

    // ==========================
    // Profile Page
    // ==========================
    @GetMapping("/profile")
    public String profile(HttpSession session, Model model) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        User user = getLoggedInUser(session);
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    // ==========================
    // Update Profile
    // ==========================
    @PostMapping("/profile/update")
    public String updateProfile(@ModelAttribute User updatedUser,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            User currentUser = getLoggedInUser(session);
            if (currentUser == null) {
                return "redirect:/login";
            }

            // Update user details
            currentUser.setFullName(updatedUser.getFullName());
            currentUser.setPhone(updatedUser.getPhone());
            currentUser.setAddress(updatedUser.getAddress());

            userService.updateUser(currentUser);
            
            // Update session
            session.setAttribute("user", currentUser);

            redirectAttributes.addFlashAttribute("success", "✅ Profile updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Failed to update profile: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // ==========================
    // Change Password
    // ==========================
    @PostMapping("/profile/change-password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 HttpSession session,
                                 RedirectAttributes redirectAttributes) {
        if (!isAuthenticated(session)) {
            return "redirect:/login";
        }

        try {
            User user = getLoggedInUser(session);
            if (user == null) {
                return "redirect:/login";
            }

            // Validate current password
            if (!userService.validatePassword(user.getEmail(), currentPassword)) {
                redirectAttributes.addFlashAttribute("error", "❌ Current password is incorrect!");
                return "redirect:/profile";
            }

            // Validate new password
            if (newPassword.length() < 6) {
                redirectAttributes.addFlashAttribute("error", "❌ New password must be at least 6 characters!");
                return "redirect:/profile";
            }

            if (!newPassword.equals(confirmPassword)) {
                redirectAttributes.addFlashAttribute("error", "❌ New passwords do not match!");
                return "redirect:/profile";
            }

            userService.updatePassword(user.getEmail(), newPassword);
            redirectAttributes.addFlashAttribute("success", "✅ Password changed successfully!");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "❌ Failed to change password: " + e.getMessage());
        }

        return "redirect:/profile";
    }

    // ==========================
    // Helper Methods
    // ==========================
    private void addDashboardStats(Model model) {
        model.addAttribute("totalComplaints", complaintService.getTotalComplaints());
        model.addAttribute("pendingComplaints", complaintService.getPendingComplaints());
        model.addAttribute("resolvedComplaints", complaintService.getResolvedComplaints());
        model.addAttribute("inProgressComplaints", complaintService.getInProgressComplaints());
    }
}