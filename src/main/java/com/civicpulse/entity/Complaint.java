package com.civicpulse.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="complaints")
public class Complaint {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id", nullable=false)
    private User user;


    @Column(nullable=false)
    private String title;


    @Column(length=1000)
    private String description;


    private String category;
    private String location;

    private String status="Pending";

    private String priority="MEDIUM";


    @Column(name="assigned_officer")
    private String assignedOfficer;


    private String photoName;
    private String videoName;


    private boolean urgent=false;


    @Column(length=2000)
    private String resolutionNotes;


    @Column(name="created_at")
    private LocalDateTime createdAt;


    @Column(name="updated_at")
    private LocalDateTime updatedAt;



    public Complaint(){}


    public Complaint(String title,String description,
                     String category,String location){

        this.title=title;
        this.description=description;
        this.category=category;
        this.location=location;
    }



    @PrePersist
    public void create(){

        if(status==null)
            status="Pending";

        if(priority==null)
            priority="MEDIUM";

        createdAt=LocalDateTime.now();
        updatedAt=LocalDateTime.now();
    }



    @PreUpdate
    public void update(){

        updatedAt=LocalDateTime.now();
    }



    public Long getId(){
        return id;
    }

    public void setId(Long id){
        this.id=id;
    }


    public User getUser(){
        return user;
    }

    public void setUser(User user){
        this.user=user;
    }


    public Long getUserId(){
        return user!=null ? user.getId():null;
    }


    public String getUserEmail(){
        return user!=null ? user.getEmail():null;
    }


    public String getTitle(){
        return title;
    }

    public void setTitle(String title){
        this.title=title;
    }


    public String getDescription(){
        return description;
    }

    public void setDescription(String description){
        this.description=description;
    }


    public String getCategory(){
        return category;
    }

    public void setCategory(String category){
        this.category=category;
    }


    public String getLocation(){
        return location;
    }

    public void setLocation(String location){
        this.location=location;
    }


    public String getStatus(){
        return status;
    }

    public void setStatus(String status){
        this.status=status;
    }


    public String getPriority(){
        return priority;
    }

    public void setPriority(String priority){
        this.priority=priority;
    }


    public String getAssignedOfficer(){
        return assignedOfficer;
    }

    public void setAssignedOfficer(String assignedOfficer){
        this.assignedOfficer=assignedOfficer;
    }


    public String getPhotoName(){
        return photoName;
    }

    public void setPhotoName(String photoName){
        this.photoName=photoName;
    }


    public String getVideoName(){
        return videoName;
    }

    public void setVideoName(String videoName){
        this.videoName=videoName;
    }


    public boolean isUrgent(){
        return urgent;
    }

    public void setUrgent(boolean urgent){
        this.urgent=urgent;
    }


    public String getResolutionNotes(){
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes){
        this.resolutionNotes=resolutionNotes;
    }


    public LocalDateTime getCreatedAt(){
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt=createdAt;
    }


    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt=updatedAt;
    }


    @Override
    public String toString(){

        return "Complaint{id="+id+
                ", title='"+title+
                "', status='"+status+
                "', priority='"+priority+
                "'}";
    }
}