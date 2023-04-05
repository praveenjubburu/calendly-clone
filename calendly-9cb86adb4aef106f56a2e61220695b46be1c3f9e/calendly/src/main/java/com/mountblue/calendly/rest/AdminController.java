package com.mountblue.calendly.rest;

import com.mountblue.calendly.Entity.ActivityLog;
import com.mountblue.calendly.Entity.UserInfo;
import com.mountblue.calendly.dao.*;
import com.mountblue.calendly.email.CalenderEmail;
import com.mountblue.calendly.email.EmailSender;
import com.mountblue.calendly.security.UserOAuth2;
import com.mountblue.calendly.service.UserInfoService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class AdminController {
    @Autowired
    private EventsRepository eventsRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private BookingsRepository bookingsRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private CalenderEmail calenderEmail;

    @Autowired
    private ActivityLogRepository activityLogRepository;


   @RequestMapping("/organization")
    public String adminManagement(Model model,Authentication authentication){

         UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

         UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

         List<UserInfo> members=new ArrayList<>();

         if(userInfo.getRole().equals("OWNER")){

             members.addAll(userInfo.getOrganisationMembers());
             members.add(userInfo);

         }else{

             Optional<UserInfo> ownerOfTheOrganization= Optional.ofNullable(userInfo.getOwnerOfOrganisation());
             if(ownerOfTheOrganization.isPresent()){
                 members.addAll((Collection<? extends UserInfo>) ownerOfTheOrganization.get().getOrganisationMembers());

             }else{
                 members.add(userInfo);
             }
         }
         model.addAttribute("userRole",userInfo.getRole());
         model.addAttribute("id",userInfo.getId());
         model.addAttribute("users",members);

         return "adminManagementDashBoard";
   }
   @RequestMapping("/organization/new")
   public String addNewUser(){

       return "addEmailForNewMember";

   }
   @RequestMapping("/organization/emails")
    public String processMails(@RequestParam("emails") String emails,Model model){

       String values[]=emails.split(",");

       List<UserInfo> usersAvailable=new ArrayList<>();

       List<UserInfo> usersNotAvailable=new ArrayList<>();

       List<String> notMemberOfCalendly=new ArrayList<>();

       for(String email:values){

           Optional<UserInfo> userInfo= userInfoRepository.findByEmail(email);

           if(userInfo.isPresent()){

               if(userInfo.get().getRole().equals("USER") && userInfo.get().getStatus().equals("NotActive")){
                   usersAvailable.add(userInfo.get());
               }else{
                   usersNotAvailable.add(userInfo.get());
               }
           }else{
               notMemberOfCalendly.add(email);
           }

       }

       model.addAttribute("usersNotAvailable",usersNotAvailable);
       model.addAttribute("users",usersAvailable);
       model.addAttribute("notMemberOfCalendly",notMemberOfCalendly);

       return "setRolesForUsers";

   }

   @RequestMapping("/organization/roles")
    public String assignRolesForSelectedUsers(@RequestParam Map<String,String> map, HttpServletRequest httpServletRequest, Authentication authentication) throws MessagingException, jakarta.mail.MessagingException {

       UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

       UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

       UserInfo owner=null;

       if(userInfo.getRole().equals("OWNER")){
           owner=userInfo;
       }else{
           if(userInfo.getOwnerOfOrganisation()==null){
               owner=userInfo;
           }else{
               owner=userInfo.getOwnerOfOrganisation();
           }
       }

       for(Map.Entry<String,String> x:map.entrySet()){

           Optional<UserInfo> user=userInfoRepository.findById(Integer.parseInt(x.getKey()));

           if(user.isPresent()){

               String requestLink=(httpServletRequest.getRequestURL().toString()).replace(httpServletRequest.getServletPath()," ")+"/"+"invitation/"+user.get().getEmail()+"/"+owner.getEmail()+"/"+x.getValue();
               String body="<html >\n" +
                       "<head>\n" +
                       "</head>\n" +
                       "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                       "    <div style='margin-left:10%'>\n" +
                       "    <div style='border:1px solid white'>\n" +
                       "    <h4>hi <span>"+user.get().getName()+",</span></h4>\n" +
                       "    <h4 style='font-size:25px;'>Invitation To Join Organization</h4>\n" +
                       "    <h2>Invite From :</h2>\n" +
                       "    <h4>"+userInfo.getName()+"</h4>\n" +
                       "    <h2>Role :</h2>\n" +
                       "    <h4>"+x.getValue()+"</h4>\n" +
                       "    <h2>Accept The Invitation<h2>\n" +
                       "    <a href="+requestLink+">Link</h4>\n" +
                       "    <h2>Date :</h2>\n" +
                       "    <h4>"+new Date().toString()+"</h4>\n" +
                       "    </div>    \n" +
                       "</div>\n" +
                       "</body>\n" +
                       "</html>";
               emailSender.sendHtmlMail(user.get().getEmail(),"Calendly",body,userInfo.getEmail());
           }
       }
        return "addingNewUserDone";
   }
   @RequestMapping("/organization/search")
    public String searchPeopleInTheOrganization(@RequestParam("search") String word,Model model,Authentication authentication){

       UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

       UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

       List<UserInfo> members=new ArrayList<>();

       if(userInfo.getRole().equals("OWNER")){
           members.addAll(userInfo.getOrganisationMembers());
           members.add(userInfo);
       }else{
           Optional<UserInfo> ownerOfTheOrganization= Optional.ofNullable(userInfo.getOwnerOfOrganisation());
           if(ownerOfTheOrganization.isPresent()){

               members.addAll((Collection<? extends UserInfo>) ownerOfTheOrganization.get().getOwnerOfOrganisation());

           }else{
               members.add(userInfo);
           }
       }

       List<UserInfo> searchedList=new ArrayList<>();
       for(UserInfo user:members){

           Pattern pattern = Pattern.compile("\\b" + word + "\\b", Pattern.CASE_INSENSITIVE);

           Matcher matcher = pattern.matcher(user.getName());

           if (matcher.find()) {
              searchedList.add(user);
           }
       }
       model.addAttribute("users",searchedList);
       return "adminManagementDashBoard";
   }
   @RequestMapping("/organization/changeRole/{id}")
    public String editRoleOfTheUser(@RequestParam("role") String role, @PathVariable("id") String userId,Authentication authentication,ActivityLog activityLog){

       UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

       UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

       activityLog.setId(0);

       activityLog.setCategory("User Management");

       activityLog.setActivity("Modified");

       activityLog.setByWhom(userInfo.getName());

       activityLog.setDateAndTime(new Date());

       activityLog.setUserInfo(userInfo);

       activityLogRepository.save(activityLog);

       System.out.println(activityLog.getId());

       Optional<UserInfo> user=userInfoService.findById(Integer.parseInt(userId));

       user.get().setRole(role);

       userInfoRepository.save(user.get());

       return "redirect:/organization";
       
   }

   @RequestMapping("/organization/remove/{id}")
    public String removeUserFromTheOrganization(@PathVariable("id") String id){

       Optional<UserInfo> userInfo=userInfoRepository.findById(Integer.parseInt(id));

       if(userInfo.isPresent()){
           userInfo.get().setOwnerOfOrganisation(null);
           userInfo.get().setStatus("NotActive");
           userInfo.get().setRole("USER");
           userInfoRepository.save(userInfo.get());
       }

       return "redirect:/organization";
   }

   @RequestMapping("/organization/activityLog")
    public String activityLog(Authentication authentication,Model model){

       UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

       UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

       List<ActivityLog> activityLogs=new ArrayList<>();
       if(userInfo.getOwnerOfOrganisation()!=null){
           Set<UserInfo> userInfoList= userInfo.getOwnerOfOrganisation().getOrganisationMembers();
           userInfoList.add(userInfo.getOwnerOfOrganisation());
           for(UserInfo user:userInfoList){
               activityLogs.addAll(user.getActivityLogs());
           }
       }else{
           Set<UserInfo> userInfoList=  userInfo.getOrganisationMembers();
           userInfoList.add(userInfo);
           for(UserInfo user:userInfoList){
               activityLogs.addAll(user.getActivityLogs());
           }
       }
       model.addAttribute("activityLogs",activityLogs);

       return "ActivityLog";
   }
    @RequestMapping("/invitation/{guest}/{host}/{role}")
    public String organizationAcceptation(@PathVariable("host") String host,@PathVariable("guest") String guest, @PathVariable("role") String role, Authentication authentication,Model model){

        UserInfo owner=userInfoRepository.findByEmail(host).get();
        UserInfo user=userInfoRepository.findByEmail(guest).get();




        model.addAttribute("userId",user.getId());
        model.addAttribute("ownerId",owner.getId());
        model.addAttribute("sender",owner.getName());
        model.addAttribute("role",role);

        return "Invitation";
    }
    @RequestMapping("/invitation/response/accept/{ownerId}/{userId}/{role}")
    public String invitationAccept(@PathVariable("userId") String userId,@PathVariable("ownerId") String ownerId,@PathVariable("role") String role){
        UserInfo owner=userInfoRepository.findById(Integer.parseInt(ownerId)).get();
        UserInfo user=userInfoRepository.findById(Integer.parseInt(userId)).get();


        user.setRole(role);
        user.setStatus("Active");
        user.setOwnerOfOrganisation(owner);
        owner.setRole("OWNER");

        userInfoRepository.save(owner);
        userInfoRepository.save(user);

        return "InvitationResponse";
    }
    @RequestMapping("/invitation/response/reject/{ownerId}/{userId}/{role}")
    public String invitationReject(@PathVariable("userId") String userId,@PathVariable("ownerId") String ownerId,@PathVariable("role") String role){

        UserInfo owner=userInfoRepository.findById(Integer.parseInt(ownerId)).get();
        UserInfo user=userInfoRepository.findById(Integer.parseInt(userId)).get();

        user.setOwnerOfOrganisation(null);
        userInfoRepository.save(user);

        return "InvitationResponse";

    }
    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }

    @RequestMapping("/organization/create")
    public String createOrganization(Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

        UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

        if((userInfo.getRole()).equals("OWNER") || (userInfo.getRole()).equals("ADMIN") || ((userInfo.getStatus()).equals("Active"))){

            return "redirect:/organization";

        }else{

            return "/createOrganization";

        }

    }
    @RequestMapping("/organization/create/new")
    public String createNewOrganization(Model model,Authentication authentication,@RequestParam("organization-name") String organizationName){

        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();

        UserInfo userInfo= userInfoService.findByEmail(userOAuth2.getMail());

        userInfo.setRole("OWNER");

        userInfo.setOrganizationName(organizationName);

        userInfoRepository.save(userInfo);

        return "redirect:/organization";

    }
}
