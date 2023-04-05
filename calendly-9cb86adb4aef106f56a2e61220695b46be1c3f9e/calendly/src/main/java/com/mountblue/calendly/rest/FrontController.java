package com.mountblue.calendly.rest;

import com.mountblue.calendly.Entity.UserInfo;
import com.mountblue.calendly.dao.BookingsRepository;
import com.mountblue.calendly.dao.EventsRepository;
import com.mountblue.calendly.dao.ScheduleRepository;
import com.mountblue.calendly.Entity.Bookings;
import com.mountblue.calendly.Entity.Event;
import com.mountblue.calendly.Entity.Schedule;
import com.mountblue.calendly.dao.UserInfoRepository;
import com.mountblue.calendly.email.EmailSender;
import com.mountblue.calendly.security.UserOAuth2;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class FrontController {
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

    @RequestMapping("/home")
    public String home() {
        return "calendly";
    }
    @RequestMapping("/dashboard")
    public String dashboard(Model model,Authentication authentication) {

        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        Optional<UserInfo> userInfo=userInfoRepository.findByEmail(mail);
        List<Event> events=userInfo.get().getEvents();

        System.out.println(events);
        model.addAttribute("events",events);
        model.addAttribute("userId",userInfo.get().getId());

        return "eventType";
    }
    @RequestMapping("/login")
    public String login(@RequestParam("email") String email, RedirectAttributes redirectAttributes) {
        Optional<UserInfo> users = Optional.ofNullable(userInfoRepository.findByEmail(email).get());
        if (users.isEmpty()) {
            return "redirect:/signup";
        } else {
            int userId = users.get().getId();
            redirectAttributes.addAttribute("userId", userId);
            return "redirect:/dashboard";
        }
    }
     @RequestMapping("/signup")
     public String signUp() {
        return "SignUp";
    }

     @RequestMapping("/loginPage")
     public String loginPage(){
        return "logIn";
    }
     @RequestMapping("/calendly")
     public String calendly(){
       return "calendly";
     }

    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }

    @RequestMapping("/newFeature")
    public String weAreWorkingForThisFeature(){
        return "newFeature";
    }
    @RequestMapping("/aboutUs")
    public String aboutUs(){
        return "aboutUs";
    }
}