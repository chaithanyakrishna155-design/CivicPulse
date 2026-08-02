package com.civicpulse.service;

import com.civicpulse.entity.User;
import com.civicpulse.repository.UserRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserService {
	 private static final Logger log =
	            LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;


    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    private NotificationService notificationService;




    // =====================================================
    // FIND USERS
    // =====================================================


    public User findByEmail(String email) {

        if(email == null || email.isEmpty())
            return null;


        return userRepository
                .findByEmail(email)
                .orElse(null);

    }




    public User findById(Long id) {

        if(id == null)
            return null;


        return userRepository
                .findById(id)
                .orElse(null);

    }




    public List<User> findAllUsers(){

        return userRepository.findAll();

    }





    public List<User> findByRole(String role){

        return userRepository.findByRole(role);

    }




    public List<User> getAllOfficers(){

        return userRepository.findAllOfficers();

    }




    public List<User> findApprovedOfficers(){

        return userRepository.findApprovedOfficers();

    }




    public List<User> findPendingOfficers(){

        return userRepository.findPendingOfficers();

    }







    // =====================================================
    // REGISTER USER
    // =====================================================



    @Transactional
    public User registerUser(User user){

        try {

            if(user == null){

                log.warn("Registration failed : User object null");
                return null;

            }


            if(userRepository.existsByEmail(user.getEmail())){

                log.warn(
                    "Email already exists : {}",
                    user.getEmail()
                );

                return null;
            }



            // Encrypt password
            user.setPassword(
                    passwordEncoder.encode(
                            user.getPassword()
                    )
            );



            // Default role
            if(user.getRole()==null){

                user.setRole("CITIZEN");

            }



            // Account status
            user.setActive(true);



            // Approval logic
            if("OFFICER".equalsIgnoreCase(user.getRole())){

                user.setApproved(false);

            }
            else{

                user.setApproved(true);

            }



            user.setCreatedAt(
                    LocalDateTime.now()
            );


            user.setUpdatedAt(
                    LocalDateTime.now()
            );



            User saved =
                    userRepository.save(user);



            sendWelcomeEmail(saved);



            log.info(
                "User registered : {}",
                saved.getEmail()
            );



            return saved;



        }
        catch(Exception e){

            log.error(
                "Registration error : {}",
                e.getMessage()
            );

            return null;

        }

    }






    // =====================================================
    // LOGIN USER
    // =====================================================


    public User loginUser(
            String email,
            String password){


        try{


            Optional<User> optional =
                    userRepository.findByEmail(email);



            if(optional.isEmpty()){


                log.warn(
                    "User not found : {}",
                    email
                );


                return null;

            }




            User user =
                    optional.get();



            // Fix old NULL values
            if(user.getActive()==null){

                user.setActive(true);
                userRepository.save(user);

            }



            if(!user.getActive()){


                log.warn(
                    "Inactive account : {}",
                    email
                );


                return null;

            }




            boolean matched =
                    passwordEncoder.matches(
                            password,
                            user.getPassword()
                    );



            if(!matched){


                log.warn(
                    "Invalid password : {}",
                    email
                );


                return null;

            }





            user.setLastLogin(
                    LocalDateTime.now()
            );


            userRepository.save(user);



            log.info(
                "Login success : {}",
                email
            );


            return user;



        }
        catch(Exception e){


            log.error(
                "Login error : {}",
                e.getMessage()
            );


            return null;

        }

    }






    // =====================================================
    // PROMOTE USER TO OFFICER
    // =====================================================



    @Transactional
    public boolean makeOfficer(String email){



        try{


            User user =
                    findByEmail(email);




            if(user==null){


                log.warn(
                    "Officer user not found : {}",
                    email
                );


                return false;

            }





            user.setRole("OFFICER");

            user.setApproved(true);

            user.setUpdatedAt(
                    LocalDateTime.now()
            );



            userRepository.save(user);



            sendOfficerPromotionEmail(user);



            log.info(
                "Promoted to officer : {}",
                email
            );



            return true;



        }
        catch(Exception e){


            log.error(
                "Officer promotion failed : {}",
                e.getMessage()
            );


            return false;

        }


    }








    // =====================================================
    // REMOVE OFFICER ROLE
    // =====================================================



    @Transactional
    public boolean removeOfficerRole(Long userId){


        try{


            User user =
                    findById(userId);



            if(user==null)
                return false;





            user.setRole("CITIZEN");

            user.setApproved(false);


            user.setUpdatedAt(
                    LocalDateTime.now()
            );



            userRepository.save(user);



            return true;


        }
        catch(Exception e){


            log.error(
                "Remove officer failed : {}",
                e.getMessage()
            );


            return false;

        }


    }
    // =====================================================
    // UPDATE USER PROFILE
    // =====================================================


    @Transactional
    public User updateUser(User user){


        try{


            User existing =
                    findById(user.getId());



            if(existing == null){

                log.warn(
                    "User not found for update : {}",
                    user.getId()
                );

                return null;

            }




            existing.setFullName(
                    user.getFullName()
            );


            existing.setPhone(
                    user.getPhone()
            );


            existing.setAddress(
                    user.getAddress()
            );


            existing.setProfilePicture(
                    user.getProfilePicture()
            );


            existing.setUpdatedAt(
                    LocalDateTime.now()
            );




            User updated =
                    userRepository.save(existing);



            log.info(
                "User updated : {}",
                updated.getEmail()
            );



            return updated;


        }
        catch(Exception e){


            log.error(
                "Update user failed : {}",
                e.getMessage()
            );


            return null;

        }


    }








    // =====================================================
    // UPDATE PASSWORD
    // =====================================================



    @Transactional
    public boolean updatePassword(
            String email,
            String newPassword){


        try{


            User user =
                    findByEmail(email);



            if(user==null)
                return false;




            user.setPassword(
                    passwordEncoder.encode(
                            newPassword
                    )
            );



            user.setUpdatedAt(
                    LocalDateTime.now()
            );



            userRepository.save(user);



            log.info(
                "Password updated : {}",
                email
            );



            return true;



        }
        catch(Exception e){


            log.error(
                "Password update failed : {}",
                e.getMessage()
            );


            return false;

        }


    }








    // =====================================================
    // VALIDATE PASSWORD
    // =====================================================



    public boolean validatePassword(
            String email,
            String password){


        User user =
                findByEmail(email);



        if(user==null)
            return false;



        return passwordEncoder.matches(
                password,
                user.getPassword()
        );

    }








    // =====================================================
    // ACTIVE / INACTIVE USER
    // =====================================================



    @Transactional
    public boolean activateUser(Long id){

        return updateActiveStatus(
                id,
                true
        );

    }





    @Transactional
    public boolean deactivateUser(Long id){

        return updateActiveStatus(
                id,
                false
        );

    }





    private boolean updateActiveStatus(
            Long id,
            boolean status){


        try{


            User user =
                    findById(id);



            if(user==null)
                return false;




            user.setActive(status);

            user.setUpdatedAt(
                    LocalDateTime.now()
            );



            userRepository.save(user);



            return true;


        }
        catch(Exception e){


            log.error(
                "Status update failed : {}",
                e.getMessage()
            );


            return false;

        }


    }









    // =====================================================
    // CHECK EXISTENCE
    // =====================================================



    public boolean emailExists(String email){

        return userRepository.existsByEmail(email);

    }





    public boolean phoneExists(String phone){

        return userRepository.existsByPhone(phone);

    }








    // =====================================================
    // USER STATISTICS
    // =====================================================



    public long getTotalUsers(){

        return userRepository.count();

    }




    public long getActiveUsers(){

        return userRepository.countByActiveTrue();

    }





    public long getInactiveUsersCount(){

        return userRepository.countByActiveFalse();

    }





    public long getOfficerCount(){

        return userRepository.countByRole("OFFICER");

    }





    public long getCitizenCount(){

        return userRepository.countByRole("CITIZEN");

    }





    public long getApprovedOfficerCount(){

        return userRepository
                .countByRoleAndApprovedTrue(
                        "OFFICER"
                );

    }





    public long getPendingOfficerCount(){

        return userRepository
                .countByRoleAndApprovedFalse(
                        "OFFICER"
                );

    }









    // =====================================================
    // SEARCH USERS
    // =====================================================



    public List<User> searchUsers(String keyword){

        return userRepository.searchUsers(keyword);

    }








    // =====================================================
    // INACTIVE USERS
    // =====================================================



    public List<User> getInactiveUsers(){


        LocalDateTime date =
                LocalDateTime.now()
                .minusDays(30);



        return userRepository
                .findInactiveUsers(date);


    }








    // =====================================================
    // DELETE USER
    // =====================================================



    @Transactional
    public boolean deleteUser(Long id){


        try{


            if(!userRepository.existsById(id)){

                return false;

            }



            userRepository.deleteById(id);



            log.info(
                "User deleted : {}",
                id
            );



            return true;


        }
        catch(Exception e){


            log.error(
                "Delete failed : {}",
                e.getMessage()
            );


            return false;

        }


    }








    // =====================================================
    // NOTIFICATIONS
    // =====================================================



    private void sendWelcomeEmail(User user){


        try{


            Map<String,Object> data =
                    new HashMap<>();



            data.put(
                "title",
                "Welcome to CivicPulse"
            );


            data.put(
                "message",
                "Welcome "
                + user.getFullName()
            );


            data.put(
                "role",
                user.getRole()
            );


            data.put(
                "link",
                "/dashboard"
            );




            notificationService
            .sendEmailNotification(
                    user.getId().toString(),
                    user.getEmail(),
                    "Welcome to CivicPulse",
                    "email/welcome",
                    data
            );



        }
        catch(Exception e){


            log.error(
                "Welcome mail failed : {}",
                e.getMessage()
            );


        }


    }







    private void sendOfficerPromotionEmail(
            User user){


        try{


            Map<String,Object> data =
                    new HashMap<>();



            data.put(
                "title",
                "Officer Access Granted"
            );


            data.put(
                "message",
                "You are now a CivicPulse officer."
            );


            data.put(
                "link",
                "/officer/dashboard"
            );




            notificationService
            .sendEmailNotification(
                    user.getId().toString(),
                    user.getEmail(),
                    "Officer Access Granted",
                    "email/officer-approval",
                    data
            );



        }
        catch(Exception e){


            log.error(
                "Officer mail failed : {}",
                e.getMessage()
            );


        }


    }



}