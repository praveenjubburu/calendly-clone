package com.mountblue.calendly.rest;
import com.mountblue.calendly.Entity.*;
import com.mountblue.calendly.dao.*;
import com.mountblue.calendly.security.UserOAuth2;
import org.springframework.security.core.Authentication;
import com.mountblue.calendly.email.EmailSender;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleController {
    @Autowired
    private EventsRepository eventsRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private BookingsRepository bookingsRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private EmailSender emailSender;

    @Autowired
    private ActivityLogRepository activityLogRepository;


    @GetMapping("/addSchedule")
    public String saveSchedule(@ModelAttribute Schedule schedule, Model model, Authentication authentication, ActivityLog activityLog){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String email=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(email).get().getId();
        UserInfo userInfo=userInfoRepository.findByEmail(email).get();
        schedule.setUser(userInfo);
        scheduleRepository.save(schedule);
        {     //  for activity  log iam saving the data

            activityLog.assign(0,"Schedule","created", userInfo.getName(),new Date());
            activityLog.setUserInfo(userInfo);
            activityLogRepository.save(activityLog);

        }
        model.addAttribute("userId",userId);
        return "redirect:/dashboard";
    }

    @RequestMapping("/schedule")
    public String schedule(@ModelAttribute Schedule schedule, Model model, Authentication authentication){
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        int userId=userInfoRepository.findByEmail(userOAuth2.getMail()).get().getId();
        model.addAttribute("userId",userId);
        return "Schedule";
    }
    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }
    @RequestMapping("/reschedule/{eventId}/{bookingId}")
    public String reschedule(@PathVariable("eventId")int eventId, @PathVariable("bookingId") int bookingId, Model model)
    {
        Optional<Event> event=eventsRepository.findById(eventId);
        Date fromDate= event.get().getFromDate();
        Date toDate=event.get().getToDate();
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Bookings bookings=bookingsRepository.findByEventAndUser(eventId,bookingId);
        Date date = new Date();  // create a new Date object with the current date and time
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS");
        String formattedDate = formatter.format(date);
        model.addAttribute("booking", bookings);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",formattedDate);
        model.addAttribute("eventId",eventId);
        model.addAttribute("value",1);
        model.addAttribute("bookingId",bookingId);
        model.addAttribute("fetch",1);
        model.addAttribute("eventType",event.get().getEventType());
        return "calender";
    }
}