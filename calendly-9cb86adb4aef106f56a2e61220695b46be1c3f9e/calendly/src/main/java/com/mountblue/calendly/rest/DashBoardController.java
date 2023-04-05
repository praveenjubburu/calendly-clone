package com.mountblue.calendly.rest;

import com.mountblue.calendly.Entity.Bookings;
import com.mountblue.calendly.Entity.Event;
import com.mountblue.calendly.Entity.UserInfo;
import com.mountblue.calendly.dao.BookingsRepository;
import com.mountblue.calendly.dao.EventsRepository;
import com.mountblue.calendly.dao.ScheduleRepository;
import com.mountblue.calendly.dao.UserInfoRepository;
import com.mountblue.calendly.email.EmailSender;
import com.mountblue.calendly.security.UserOAuth2;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.mail.MessagingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
@Controller
@AllArgsConstructor
@NoArgsConstructor
public class DashBoardController {
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

    @RequestMapping("/upComing")
    public String upComingData(Model model, Authentication authentication)
    {
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String email=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(email).get().getId();
        UserInfo userInfo=userInfoRepository.findByEmail(email).get();
        List<Event> event=userInfo.getEvents();
        List<Long> eventIds=new ArrayList<>();
        List<Bookings> bookings=new ArrayList<>();
        for(Event eventData:event)
        {
            eventIds.add(eventData.getId());
            bookings.addAll(eventData.getBookings());
        }
        Date currentDate=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(currentDate);
        List<Long> bookingIdUpcoming=new ArrayList<>();
        List<Long> toDayId=new ArrayList<>();
        List<Long> collectiveId=new ArrayList<>();
        List<Long> oneOnOneId=new ArrayList<>();
        List<Long> groupId=new ArrayList<>();
        List<Long> roundRobinId=new ArrayList<>();
        for(Bookings booking:bookings)
        {
            int result=formattedDate.compareTo(String.valueOf(booking.getDate()));
            if(result<=-1){
                bookingIdUpcoming.add(booking.getId());
            }
            if(result==-11)
            {
                toDayId.add(booking.getId());
            }
            if(booking.getEvent().getEventType().equals("Collective"))
            {
                collectiveId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("OneOnOne")) {
                oneOnOneId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("Group")) {
                groupId.add(booking.getId());
            }else {
                roundRobinId.add(booking.getId());
            }
        }
        List<Bookings> bookingsData=new LinkedList<>();
        for(Long id:bookingIdUpcoming)
        {
            bookingsData.add(bookingsRepository.findById(id));
        }
        Collections.sort(bookingsData, new Comparator<Bookings>() {
            public int compare(Bookings m1, Bookings m2) {
                return m1.getDate().compareTo(m2.getDate());
            }
        });
        System.out.println(userInfo.getId());
        model.addAttribute("Today",toDayId);
        model.addAttribute("bookingsData",bookingsData);
        model.addAttribute("value","upComing");
        model.addAttribute("userId",userId);
        model.addAttribute("collectiveId",collectiveId);
        model.addAttribute("oneOnOneId",oneOnOneId);
        model.addAttribute("groupId",groupId);
        model.addAttribute("roundRobinId",roundRobinId);
        return "ScheduledEvents";
    }
    @RequestMapping("/Past")
    public String pastData(Model model, Authentication authentication)
    {
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String email=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(email).get().getId();
        UserInfo userInfo=userInfoRepository.findByEmail(email).get();
        List<Event> event=userInfo.getEvents();
        List<Long> eventIds=new ArrayList<>();
        List<Bookings> bookings=new ArrayList<>();
        List<Long> collectiveId=new ArrayList<>();
        List<Long> oneOnOneId=new ArrayList<>();
        List<Long> groupId=new ArrayList<>();
        List<Long> roundRobinId=new ArrayList<>();
        for(Event eventData:event)
        {
            eventIds.add(eventData.getId());
            bookings.addAll(eventData.getBookings());
        }
        Date currentDate=new Date();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormat.format(currentDate);
        List<Long> bookingIdPast=new ArrayList<>();
        Collections.sort(bookingIdPast);
        for(Bookings booking:bookings)
        {
            int result=formattedDate.compareTo(String.valueOf(booking.getDate()));
            if(result>=1){
                bookingIdPast.add(booking.getId());
            }
            if(booking.getEvent().getEventType().equals("Collective"))
            {
                collectiveId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("OneOnOne")) {
                oneOnOneId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("Group")) {
                groupId.add(booking.getId());
            }else {
                roundRobinId.add(booking.getId());
            }
        }
        List<Bookings> bookingsData=new LinkedList<>();
        for(Long id:bookingIdPast)
        {
            bookingsData.add(bookingsRepository.findById(id));
        }
        Collections.sort(bookingsData, new Comparator<Bookings>() {
            public int compare(Bookings m1, Bookings m2) {
                return m1.getDate().compareTo(m2.getDate());
            }
        });
        List<Long> toDayId=new ArrayList<>();
        model.addAttribute("Today",toDayId);
        model.addAttribute("bookingsData",bookingsData);
        model.addAttribute("value","Past");
        model.addAttribute("userId",userId);
        model.addAttribute("collectiveId",collectiveId);
        model.addAttribute("oneOnOneId",oneOnOneId);
        model.addAttribute("groupId",groupId);
        model.addAttribute("roundRobinId",roundRobinId);
        return "ScheduledEvents";
    }
    @RequestMapping("/call/{id}/{value}")
    public String call(@PathVariable("id")long id,@PathVariable("value")String value, Model model)
    {
        Bookings bookings=bookingsRepository.findById(id);
        String email=bookings.getEmail();
        Date date=bookings.getDate();
        String timeZone=bookings.getTimeZone();
        Long eventId=bookings.getEvent().getId();
        Event event=eventsRepository.findById(eventId);
        model.addAttribute("email",email);
        model.addAttribute("date",date);
        model.addAttribute("event",event);
        model.addAttribute("timeZone",timeZone);
        model.addAttribute("name",bookingsRepository.findById(id).getName());
        model.addAttribute("value",value);
        return "Details";
    }
    @RequestMapping("/currentDelete/{id}")
    public String currentDelete(@PathVariable("id")Long id) throws MessagingException, jakarta.mail.MessagingException {
        Bookings bookings = bookingsRepository.findById(id);
        Event event = bookings.getEvent();
        emailSender.sendHtmlMail(bookings.getEmail(), "Deleted",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>" + bookings.getName() + ",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>event has been deleted.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>" + event.getEventType() + "</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>" + bookings.getName() + "</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>" + bookings.getEvent().getUserInfo().getEmail() + "</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>" + bookings.getTime() + " + " + bookings.getDate() + "</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>", "anilbalaga2001@gmail.com");
        bookingsRepository.deleteById(Math.toIntExact(id));
        return "redirect:/upComing";
    }
    @RequestMapping("/currentDelete/{id}/{MyBooking}")
    public String currentDelete(@PathVariable("id")Long id,@PathVariable("MyBooking")String myBooking) throws MessagingException, jakarta.mail.MessagingException {
        Bookings bookings = bookingsRepository.findById(id);
        Event event = bookings.getEvent();
        emailSender.sendHtmlMail(bookings.getEmail(), "Deleted",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>" + bookings.getName() + ",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>event has been deleted.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>" + event.getEventType() + "</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>" + bookings.getName() + "</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>" + bookings.getEvent().getUserInfo().getEmail() + "</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>" + bookings.getTime() + " + " + bookings.getDate() + "</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>", "anilbalaga2001@gmail.com");
        bookingsRepository.deleteById(Math.toIntExact(id));
        return "redirect:/myBookings";
    }

    @RequestMapping("/sortByDate")
    public String sortByDate(@RequestParam("fromDate") String fromDate, @RequestParam("toDate") String toDate, Model model, Authentication authentication) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd"); // specify the date format of the string
        Date startingDate = null;
        Date endingDate = null;
        try {
            startingDate = dateFormat.parse(fromDate);
            endingDate = dateFormat.parse(toDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        List<Long> toDayId=new ArrayList<>();
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        List<Bookings> bookingsData = bookingsRepository.sortByDate(startingDate, endingDate);
        List<Long> collectiveId=new ArrayList<>();
        List<Long> oneOnOneId=new ArrayList<>();
        List<Long> groupId=new ArrayList<>();
        List<Long> roundRobinId=new ArrayList<>();
        Date currentDate=new Date();
        SimpleDateFormat dateFormats = new SimpleDateFormat("yyyy-MM-dd");
        String formattedDate = dateFormats.format(currentDate);
        List<Long> bookingIdPast=new ArrayList<>();
        Collections.sort(bookingIdPast);
        for(Bookings booking:bookingsData)
        {
            int result=formattedDate.compareTo(String.valueOf(booking.getDate()));
            if(result>=1){
                bookingIdPast.add(booking.getId());
            }
            if(booking.getEvent().getEventType().equals("Collective"))
            {
                collectiveId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("OneOnOne")) {
                oneOnOneId.add(booking.getId());
            } else if (booking.getEvent().getEventType().equals("Group")) {
                groupId.add(booking.getId());
            }else {
                roundRobinId.add(booking.getId());
            }
        }
        model.addAttribute("collectiveId",collectiveId);
        model.addAttribute("oneOnOneId",oneOnOneId);
        model.addAttribute("groupId",groupId);
        model.addAttribute("roundRobinId",roundRobinId);
        model.addAttribute("Today", toDayId);
        model.addAttribute("bookingsData", bookingsData);
        model.addAttribute("value", "upComing");
        return "ScheduledEvents";
    }
    @RequestMapping("/accountSettings")
    public String accountSettings(Model model, Authentication authentication)
    {
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String email=userOAuth2.getMail();
        UserInfo userInfo=userInfoRepository.findByEmail(email).get();
        model.addAttribute("userInfo",userInfo);
        model.addAttribute("userId",userInfo.getId());
        return "accountSettings";

    }
    @RequestMapping("/saveAccountSettings")
    public String saveAccountSettings(Model model,@ModelAttribute("userInfo")UserInfo userInfo,@RequestParam("userId")int userId)
    {
        Optional<UserInfo> userInfoData=userInfoRepository.findById(userId);
        userInfo.setTimeZone(userInfo.getTimeZone());
        userInfo.setMessage(userInfo.getMessage());
        userInfo.setEmail(userInfoData.get().getEmail());
        userInfo.setRole(userInfoData.get().getRole());
        userInfo.setOwnerOfOrganisation(userInfoData.get().getOwnerOfOrganisation());
        userInfo.setId(userId);
        userInfo.setName(userInfo.getName());
        userInfo.setCountry(userInfo.getCountry());
        userInfoRepository.save(userInfo);
        return "accountSettings";
    }
    @RequestMapping("/deleteUser/{userId}")
    public String deleteUser(@PathVariable("userId")int userId)
    {
        Optional<UserInfo> userInfo= userInfoRepository.findById(userId);
        userInfoRepository.delete(userInfo.get());
        return "calendly";
    }
    @RequestMapping("/google")
    public String google(@RequestParam("email") String email,Model model)
    {
        Optional<UserInfo> user=userInfoRepository.findByEmail(email);
        if(user.isEmpty()){
            model.addAttribute("value",1);
            model.addAttribute("message","No account exists for "+email);
            return "logIn";
        }

        return "loginToGoogle";
    }

    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }
}