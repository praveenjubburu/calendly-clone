package com.mountblue.calendly.rest;

import com.mountblue.calendly.Entity.*;
import com.mountblue.calendly.dao.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import com.mountblue.calendly.email.EmailSender;
import com.mountblue.calendly.security.UserOAuth2;
import com.mountblue.calendly.service.ScheduleService;
import com.mountblue.calendly.service.UserInfoService;
import com.mountblue.calendly.service.UserInfoServiceImplementation;
import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class EventsController {         //This Controller only for events related work update,delete,mainPage

    @Autowired
    private EventsRepository eventsRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private BookingsRepository bookingsRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired(required = true)
    private UserInfoService userInfoService;

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private ActivityLog activityLog;
    @GetMapping("/newEvent")
    public String newEvent(@ModelAttribute Event event, Model model,Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        List<Schedule> schedules=scheduleRepository.findAll();
        System.err.println(schedules.size());
        LocalDate startDate=LocalDate.now();//edited by aravind
        model.addAttribute("startDate",startDate);
        model.addAttribute("schedules",schedules);
        System.out.println(schedules.size());
        model.addAttribute("userId",userId);
        return "oneOnone";
    }
    @PostMapping("/saveEvent")
    public String saveEvent(@ModelAttribute Event event, Model model, @RequestParam("eventType") String typeOfEvent, Authentication authentication, @RequestParam("from") String fromDate, @RequestParam("to") String toDate, @RequestParam("schedule") List<String> scheduleId, @RequestParam("eventType") String eventType, HttpServletRequest httpServletRequest) throws ParseException {
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        event.setFromDate(format.parse(fromDate));
        event.setToDate(format.parse(toDate));

        event.setEventType(typeOfEvent);

        {     //  for activity  log iam saving the data

            activityLog.assign(0,"Event Type",typeOfEvent+" "+((event.getId()==null)?"created":"Modified"),mail,new Date());
            Optional<UserInfo> currentUser=userInfoRepository.findById(userId);
            activityLog.setUserInfo(currentUser.get());
            activityLogRepository.save(activityLog);

        }

        String selectedScheduledIds="";
        for(int index=0;index<scheduleId.size();index++){     //adding shedule id's in the form of string so
            selectedScheduledIds+=scheduleId.get(index);
            if(index<scheduleId.size()-1){
                selectedScheduledIds+=",";
            }
        }

        event.setScheduleId(selectedScheduledIds);

        Optional<UserInfo> userInfo=userInfoRepository.findById(userId);
        if(userInfo.isPresent()){
            event.setUserInfo(userInfo.get());
            System.err.println(userInfo.get());
        }else{
           return ExceptionHandlers();
        }
        String finalEventLink= (httpServletRequest.getRequestURL().toString()).replace(httpServletRequest.getServletPath()," ")+"/"+mail.substring(0,mail.length()-10)+"/"+event.getEventLink();
        event.setEventLink(finalEventLink);
        event.setEventType(eventType);
        eventsRepository.save(event);
         model.addAttribute("userId",userId);
        return "redirect:/dashboard";
    }
    @RequestMapping("/eventBooking")
    public String eventBooking(@RequestParam(name = "eventId",required = false) int id) {
        String startTimeStr = "09:00";
        String endTimeStr = "22:30";
        int slotDurationMinutes = 30;
        int timeGapMinutes = 15;
        return null;
    }
    @RequestMapping("/editEvent/{id}")
    public String editEvent(@PathVariable("id") int id,Model model,Authentication authentication,ActivityLog activityLog) {
       UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        Optional<Event> event = eventsRepository.findById(id);
        List<Schedule> schedules = scheduleRepository.findAll();    //we have to change this now it's collecting all the schedules
        model.addAttribute("userId",userId);
        model.addAttribute("schedules", schedules);
        model.addAttribute("eventId",id);
        if (event.isPresent()) {
            event.get().setEventLink(null);
            model.addAttribute("event", event);
        } else {
           return ExceptionHandlers();
        }
        if(event.get().getEventType().equals("OneOnOne")) {
            return "oneOnone";
        }else if(event.get().getEventType().equals("Group"))
        {
            return "group";
        } else if (event.get().getEventType().equals("Collective")) {

            Map<String,List> hosts=new HashMap<>();

            String scheduledIds=event.get().getScheduleId();

            String scheduledIdSplit[]=scheduledIds.split(",");

            for(String scheduledId : scheduledIdSplit){

                Optional<Schedule> scheduleInTheDatabase=scheduleRepository.findById(Integer.parseInt(scheduledId));

                if(scheduleInTheDatabase.isPresent()){

                    UserInfo userInfo=scheduleInTheDatabase.get().getUserInfo();

                    List<Schedule> scheduleList=userInfo.getSchedules();

                    hosts.put(userInfo.getName(),scheduleList);
                }
            }

            model.addAttribute("hosts",hosts);

            return "collective";
        }
        else {

            Map<String,List> hosts=new HashMap<>();

            String scheduledIds=event.get().getScheduleId();

            String scheduledIdSplit[]=scheduledIds.split(",");

            for(String scheduledId : scheduledIdSplit){

                Optional<Schedule> scheduleInTheDatabase=scheduleRepository.findById(Integer.parseInt(scheduledId));

                if(scheduleInTheDatabase.isPresent()){

                    UserInfo userInfo=scheduleInTheDatabase.get().getUserInfo();

                    List<Schedule> scheduleList=userInfo.getSchedules();

                    hosts.put(userInfo.getName(),scheduleList);
                }
            }

            model.addAttribute("hosts",hosts);

            return "round_robin";
        }
    }
    @RequestMapping("/search")
    public String searchEvent(@RequestParam("search") String word,Model model, Authentication authentication){

        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        List<Event> events=eventsRepository.findByEventNameContainingIgnoreCase(word);
        model.addAttribute("events",events);
        model.addAttribute("userId",userId);
        return "eventType";
    }
    @RequestMapping("/new/{event_type}")
    public String newEvent(@PathVariable String event_type,@ModelAttribute Event event,Model model, Authentication authentication,ActivityLog activityLog){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        List<Schedule> schedules=scheduleRepository.findAll();
        model.addAttribute("schedules",schedules);
        model.addAttribute("userId",userId);

        if(event_type.equals("solo")){
            return "oneOnone";
        }else if(event_type.equals("group")){
            return "group";

        }else if(event_type.equals("collective")){

            return "redirect:/selectPeopleForCollectiveEventType";
        }else{

            return "redirect:/selectPeopleForRoundRobinEventType";
        }
    }
    @RequestMapping("/selectEventType")
    public String eventType(Model model, Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        model.addAttribute("userId",userId);

        Optional<UserInfo> currentUser=userInfoRepository.findById(userId);
        if(currentUser.isPresent()){
            if((currentUser.get().getRole().equals("OWNER") && currentUser.get().getOrganisationMembers().size()==0) || currentUser.get().getRole().equals("NotActive")){
                model.addAttribute("notInTheOrganization","true");
            }else{
                model.addAttribute("notInTheOrganization","false");
            }
        }
        return "selectEventType";
    }

    @RequestMapping("/selectPeopleForCollectiveEventType")
    public String selectPeopleForCollectiveEventType(Model model,HttpSession session, Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        UserInfo userInfo=userInfoService.findByEmail(mail);
        List<UserInfo> selectedPeopleList=new ArrayList<>();

        List<UserInfo> peopleInTheSameOrganization=new ArrayList<>();


        if(userInfo.getRole().equals("OWNER")==true){
            peopleInTheSameOrganization.addAll(userInfo.getOrganisationMembers());
            selectedPeopleList.add(userInfo);
        }
        else{
            Optional<UserInfo> ownerOfTheOrganization=userInfoService.findById(userInfo.getOwnerOfOrganisation().getId());
            if(ownerOfTheOrganization.isPresent()){
                peopleInTheSameOrganization.addAll(ownerOfTheOrganization.get().getOrganisationMembers());
            }
        }

        model.addAttribute("userInfoList",peopleInTheSameOrganization);


        session.setAttribute("selectedPeopleList",selectedPeopleList);
        LocalDate startDate=LocalDate.now();                                                          //edited by aravind
        model.addAttribute("startDate",startDate);
        model.addAttribute("selectedPeopleList",selectedPeopleList);

        return "selectPeopleForCollective";


    }
    @RequestMapping("/collective/search")
    public String SearchInCollectiveEventType(@RequestParam(value = "search",defaultValue ="") String name,  Authentication authentication,HttpSession session, Model model,@RequestParam(value = "selectedIds",required = false) List<String> selectedIds){
        List<UserInfo> selectedPeopleList= (List<UserInfo>) session.getAttribute("selectedPeopleList");
         UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        if(selectedIds!=null && selectedIds.isEmpty()!=true){
            for(String id:selectedIds){
                Optional<UserInfo> userInfo=userInfoService.findById(Integer.parseInt(id));
                if(userInfo.isPresent()){
                    selectedPeopleList.add(userInfo.get());
                }
            }

        }

        List<UserInfo> searchedListPeople=userInfoService.findByNameContaining(name.equals("")==true?"@@@":name);

        model.addAttribute("userInfoList",searchedListPeople);
        session.setAttribute("selectedPeopleList",selectedPeopleList);
        model.addAttribute("selectedPeopleList",selectedPeopleList);

        return "selectPeopleForCollective";

    }
    @RequestMapping("/collective")
    public String collectiveEventType(HttpSession session,Model model,@ModelAttribute Event event){

        List<UserInfo> selectedPeopleList= (List<UserInfo>) session.getAttribute("selectedPeopleList");

        Schedule defaultSchedule=scheduleRepository.findByName("default schedule");

        if(defaultSchedule==null){
            Schedule schedule=new Schedule();
            schedule.setName("default schedule");
            scheduleRepository.save(schedule);
            defaultSchedule=schedule;
        }

        Map<String,List> hosts=new HashMap<>();

        for(UserInfo userInfo:selectedPeopleList){
            List<Schedule> userSchedules=new ArrayList<>();
            userSchedules.addAll(userInfo.getSchedules());
            userSchedules.add(defaultSchedule);        //default schedule
            hosts.put(userInfo.getName(),userSchedules);
        }

        model.addAttribute("hosts",hosts);

        return "collective";

    }
    @RequestMapping("/selectPeopleForRoundRobinEventType")
    public String selectPeopleForRoundRobin(HttpSession session,Model model,Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        UserInfo userInfo=userInfoService.findByEmail(userOAuth2.getMail());  //changed by aravind
        List<UserInfo> selectedPeopleList=new ArrayList<>();
        List<UserInfo> peopleInTheSameOrganization=new ArrayList<>();

        if(userInfo.getRole().equals("OWNER")==true){
            peopleInTheSameOrganization.addAll(userInfo.getOrganisationMembers());
            selectedPeopleList.add(userInfo);
        }
        else{
            Optional<UserInfo> ownerOfTheOrganization=userInfoService.findById(userInfo.getOwnerOfOrganisation().getId());
            if(ownerOfTheOrganization.isPresent()){
                peopleInTheSameOrganization.addAll(ownerOfTheOrganization.get().getOrganisationMembers());
            }
        }

        model.addAttribute("userInfoList",peopleInTheSameOrganization);


        session.setAttribute("selectedPeopleList",selectedPeopleList);
        LocalDate startDate=LocalDate.now();
        model.addAttribute("startDate",startDate);
        model.addAttribute("selectedPeopleList",selectedPeopleList);

        return "selectPeopleForRoundRobin";

    }
    @RequestMapping("/RoundRobin/search")
    public String SearchInRoundRobinEventType(@RequestParam(value = "search",defaultValue ="") String name, HttpSession session, Model model,@RequestParam(value = "selectedIds",required = false) List<String> selectedIds){
        List<UserInfo> selectedPeopleList= (List<UserInfo>) session.getAttribute("selectedPeopleList");

        if(selectedIds!=null && selectedIds.isEmpty()!=true){

            for(String id:selectedIds){
                Optional<UserInfo> userInfo=userInfoService.findById(Integer.parseInt(id));
                if(userInfo.isPresent()){
                    selectedPeopleList.add(userInfo.get());
                }
            }

        }

        List<UserInfo> searchedListPeople=userInfoService.findByNameContaining(name.equals("")==true?"@@@":name);

        model.addAttribute("userInfoList",searchedListPeople);
        session.setAttribute("selectedPeopleList",selectedPeopleList);
        model.addAttribute("selectedPeopleList",selectedPeopleList);

        return "selectPeopleForRoundRobin";
    }
    @RequestMapping("/RoundRobin")
    public String RoundRobin(HttpSession session,Model model,@ModelAttribute Event event){
        List<UserInfo> selectedPeopleList= (List<UserInfo>) session.getAttribute("selectedPeopleList");

        Schedule defaultSchedule=scheduleRepository.findByName("default schedule");

        if(defaultSchedule==null){
            Schedule schedule=new Schedule();
            schedule.setName("default schedule");
            scheduleRepository.save(schedule);
            defaultSchedule=schedule;
        }

        Map<String,List> hosts=new HashMap<>();

        for(UserInfo userInfo:selectedPeopleList){
            List<Schedule> userSchedules=new ArrayList<>();
            userSchedules.addAll(userInfo.getSchedules());
            userSchedules.add(defaultSchedule);        //default schedule
            hosts.put(userInfo.getName(),userSchedules);
        }

        model.addAttribute("hosts",hosts);

        return "round_robin";

    }
    @RequestMapping("event/delete/{id}")
    public String deleteEvent(@PathVariable("id") int id,Model model,ActivityLog activityLog,Authentication authentication) throws MessagingException, jakarta.mail.MessagingException {
        Optional<Event> event=eventsRepository.findById(id);
        List<Bookings> bookings =bookingsRepository.findByEvent_id(id);
        if(event.isPresent()){
            eventsRepository.delete(event.get());
            for(Bookings booking:bookings)
            {
                System.out.println(booking.getEmail());
                emailSender.sendHtmlMail(booking.getEmail(),"Deleted",
                        "<html >\n" +
                                "<head>\n" +
                                "</head>\n" +
                                "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                                "    <div style='margin-left:10%'>\n" +
                                "    <div style='border:1px solid white'>\n" +
                                "    <h4>hi <span>"+booking.getName()+",</span></h4>\n" +
                                "    <h4 style='font-size:25px;'>Event has been Deleted.</h4>\n" +
                                "    <h2>Event Type :</h2>\n" +
                                "    <h4>"+booking.getEvent().getEventType()+"</h4>\n" +
                                "    <h2>Name :</h2>\n" +
                                "    <h4>"+booking.getName()+"</h4>\n" +
                                "    <h2>Invite Email :</h2>\n" +
                                "    <h4>"+booking.getEmail()+"</h4>\n" +
                                "    <h2>Event Date/Time :</h2>\n" +
                                "    <h4>"+booking.getTime()+"  "+booking.getDate()+"</h4>\n" +
                                "    </div>    \n" +
                                "</div>\n" +
                                "</body>\n" +
                                "</html>","anilbalaga2001@gmail.com");
            }

            {     //  for activity  log iam saving the data
                UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
                String mail=userOAuth2.getMail();
                UserInfo userInfo=userInfoService.findByEmail(mail);

                activityLog.assign(0,"Event Type",event.get().getEventType()+" "+"deleted",mail,new Date());
                activityLog.setUserInfo(userInfo);
                activityLogRepository.save(activityLog);

            }

        }else{
           return ExceptionHandlers();
        }
        return "redirect:/dashboard";
    }
    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }
    @RequestMapping("/dashboard/event_types/user/{id}")
    public String vistingAsOwner(@PathVariable ("id") String id,Model model){
          Optional<UserInfo> userInfo=userInfoRepository.findById(Integer.parseInt(id));
          List<Event> events=new ArrayList<>();
          if(userInfo.isPresent()){
              events=userInfo.get().getEvents();
          }

          model.addAttribute("events",events);
          model.addAttribute("userName",userInfo.get().getName());

          return "visitOrganizationMemberEvent";
    }

}
