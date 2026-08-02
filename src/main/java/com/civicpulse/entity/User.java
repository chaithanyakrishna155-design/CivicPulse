package com.civicpulse.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;


@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @NotBlank(message = "Full Name is required")
    @Column(nullable = false)
    private String fullName;


    @Email(message = "Enter a valid email")
    @NotBlank(message = "Email is required")
    @Column(unique = true, nullable = false)
    private String email;


    @NotBlank(message = "Phone Number is required")
    @Column(nullable = false)
    private String phone;


    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must contain at least 6 characters")
    @Column(nullable = false)
    private String password;



    @Column(nullable = false)
    private String role = "CITIZEN";



    @Column(name = "is_approved", nullable = false)
    private Boolean approved = false;



    @Column(name = "is_active", nullable = false)
    private Boolean active = true;



    @Column(length = 500)
    private String address;



    @Column(name = "profile_picture")
    private String profilePicture;



    @Column(name = "last_login")
    private LocalDateTime lastLogin;



    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;



    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;



    // ================= CONSTRUCTORS =================


    public User() {

        this.role = "CITIZEN";
        this.approved = false;
        this.active = true;

    }



    public User(Long id,
                String fullName,
                String email,
                String phone,
                String password,
                String role,
                boolean approved) {

        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.password = password;
        this.role = role;
        this.approved = approved;
        this.active = true;

    }





    // ================= LIFECYCLE =================


    @PrePersist
    public void onCreate() {


        if(role == null) {
            role = "CITIZEN";
        }


        if(approved == null) {
            approved = false;
        }


        if(active == null) {
            active = true;
        }


        if(createdAt == null) {
            createdAt = LocalDateTime.now();
        }


        if(updatedAt == null) {
            updatedAt = LocalDateTime.now();
        }

    }



    @PreUpdate
    public void onUpdate() {

        updatedAt = LocalDateTime.now();

    }





    // ================= GETTERS SETTERS =================


    public Long getId() {
        return id;
    }


    public void setId(Long id) {
        this.id = id;
    }



    public String getFullName() {
        return fullName;
    }


    public void setFullName(String fullName) {
        this.fullName = fullName;
    }



    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }



    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }



    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }



    public String getRole() {
        return role;
    }


    public void setRole(String role) {

        if(role != null) {

            String r = role.toUpperCase();


            if(!r.equals("ADMIN")
                    && !r.equals("OFFICER")
                    && !r.equals("CITIZEN")) {

                throw new IllegalArgumentException(
                    "Invalid role"
                );

            }


            this.role = r;

        }

    }



    // IMPORTANT FOR THYMELEAF / SERVICES

    public Boolean getApproved() {
        return approved;
    }


    public boolean isApproved() {
        return Boolean.TRUE.equals(approved);
    }


    public void setApproved(boolean approved) {
        this.approved = approved;
    }




    public Boolean getActive() {
        return active;
    }


    public boolean isActive() {
        return Boolean.TRUE.equals(active);
    }


    public void setActive(boolean active) {
        this.active = active;
    }




    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }



    public String getProfilePicture() {
        return profilePicture;
    }


    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }



    public LocalDateTime getLastLogin() {
        return lastLogin;
    }


    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }



    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }



    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }


    public String getRole() {
        return role;
    }


    public void setRole(String role) {

        if(role != null){

            String normalized = role.toUpperCase();

            if(!normalized.equals("ADMIN")
                    && !normalized.equals("OFFICER")
                    && !normalized.equals("CITIZEN")){

                throw new IllegalArgumentException(
                    "Role must be ADMIN, OFFICER, or CITIZEN"
                );
            }

            this.role = normalized;
        }
    }


    // IMPORTANT: Hibernate + Spring Security needs these exact names
    public Boolean getApproved() {
        return approved;
    }


    public void setApproved(Boolean approved) {
        this.approved = approved;
    }


    public Boolean getActive() {
        return active;
    }


    public void setActive(Boolean active) {
        this.active = active;
    }


    public String getAddress() {
        return address;
    }


    public void setAddress(String address) {
        this.address = address;
    }


    public String getProfilePicture() {
        return profilePicture;
    }


    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }


    public LocalDateTime getLastLogin() {
        return lastLogin;
    }


    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }


    public LocalDateTime getCreatedAt() {
        return createdAt;
    }


    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }


    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }



    // Boolean helper methods

    public boolean isApproved() {

        return Boolean.TRUE.equals(approved);

    }


    public boolean isActive() {

        return Boolean.TRUE.equals(active);

    }



    // Role checks

    public boolean isAdmin(){

        return "ADMIN".equalsIgnoreCase(role);

    }


    public boolean isOfficer(){

        return "OFFICER".equalsIgnoreCase(role);

    }


    public boolean isCitizen(){

        return "CITIZEN".equalsIgnoreCase(role);

    }



    public boolean isApprovedOfficer(){

        return isOfficer() && isApproved();

    }



    public boolean canAccessAdminPanel(){

        return isAdmin();

    }



    public boolean canAccessOfficerPanel(){

        return isOfficer() && isApproved();

    }



    public String getDisplayRole(){

        if(isAdmin())
            return "👑 Admin";


        if(isOfficer() && isApproved())
            return "👮 Officer";


        if(isOfficer())
            return "⏳ Officer Pending";


        return "👤 Citizen";

    }
    public String getRoleBadgeClass(){

        if(isAdmin())
            return "badge bg-danger";


        if(isOfficer() && isApproved())
            return "badge bg-primary";


        if(isOfficer())
            return "badge bg-warning text-dark";


        return "badge bg-secondary";
    }



    public String getStatusBadgeClass(){

        if(isActive())
            return "badge bg-success";


        return "badge bg-danger";

    }



    public String getStatusDisplay(){

        return isActive()
                ? "🟢 Active"
                : "🔴 Inactive";

    }



    public void updateLastLogin(){

        this.lastLogin = LocalDateTime.now();

    }



    // ================= BUILDER =================


    public static Builder builder(){

        return new Builder();

    }



    public static class Builder {


        private String fullName;

        private String email;

        private String phone;

        private String password;

        private String role = "CITIZEN";

        private String address;

        private String profilePicture;



        public Builder fullName(String fullName){

            this.fullName = fullName;

            return this;

        }



        public Builder email(String email){

            this.email = email;

            return this;

        }



        public Builder phone(String phone){

            this.phone = phone;

            return this;

        }



        public Builder password(String password){

            this.password = password;

            return this;

        }



        public Builder role(String role){

            this.role = role;

            return this;

        }



        public Builder address(String address){

            this.address = address;

            return this;

        }



        public Builder profilePicture(String profilePicture){

            this.profilePicture = profilePicture;

            return this;

        }



        public User build(){

            User user = new User();


            user.setFullName(fullName);

            user.setEmail(email);

            user.setPhone(phone);

            user.setPassword(password);

            user.setRole(role);

            user.setAddress(address);

            user.setProfilePicture(profilePicture);

            user.setActive(true);

            user.setApproved(false);


            return user;

        }

    }




    // ================= TO STRING =================


    @Override
    public String toString(){

        return "User{" +
                "id=" + id +
                ", fullName='" + fullName + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", approved=" + approved +
                ", active=" + active +
                '}';

    }




    // ================= EQUALS HASHCODE =================


    @Override
    public boolean equals(Object obj){

        if(this == obj)
            return true;


        if(obj == null || getClass() != obj.getClass())
            return false;


        User user = (User) obj;


        return id != null && id.equals(user.id);

    }



    @Override
    public int hashCode(){

        return id != null ? id.hashCode() : 0;

    }


}