package com.mountblue.calendly.rest;

import com.mountblue.calendly.Entity.*;
import org.springframework.security.core.Authentication;
import com.mountblue.calendly.dao.BookingsRepository;
import com.mountblue.calendly.dao.EventsRepository;
import com.mountblue.calendly.dao.ScheduleRepository;
import com.mountblue.calendly.dao.UserInfoRepository;
import com.mountblue.calendly.email.CalenderEmail;
import com.mountblue.calendly.email.EmailSender;
import com.mountblue.calendly.security.UserOAuth2;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.mail.MessagingException;
import javax.sound.midi.SysexMessage;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Controller
@AllArgsConstructor
@NoArgsConstructor
public class BookingsController {      //This Controller For Anything related to the Booking
    @Autowired
    private EventsRepository eventsRepository;
    @Autowired
    private ScheduleRepository scheduleRepository;
    @Autowired
    private BookingsRepository bookingsRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private CalenderEmail calenderEmail;

    @Autowired
    private EmailSender emailSender;



    @RequestMapping("/conformBooking")
    public String enterDetails(@RequestParam(name = "hostId",required = false) String hostId,
                               @RequestParam("eventId") int id,
                               @RequestParam(name="bookingId",defaultValue = "0",required = false)long bookingId,
                               @RequestParam(value = "time",required = false) String time,
                               Authentication authentication,
                               @RequestParam("date") String date,
                               @RequestParam("name") String guestName,
                               @RequestParam("email") String guestMail,
                               @RequestParam(name="description",required = false,defaultValue = "") String description, Model model) throws ParseException, MessagingException, jakarta.mail.MessagingException {
        System.err.println(id);
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        int userId=userInfoRepository.findByEmail(userOAuth2.getMail()).get().getId();
        Optional<Event> event=eventsRepository.findById(id);
        Bookings bookings;
        if(bookingId==0) {
            bookings = new Bookings();
        }else {
            bookings=bookingsRepository.findById(bookingId);
        }
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        bookings.setDate(dateString);
        bookings.setEmail(guestMail);
        bookings.setName(guestName);
        bookings.setDescription(description);
        bookings.setTime(time);
        bookings.setEvent(event.get());
        System.err.println(event.get().getEventType());
        System.out.println(time);
        if(event.get().getEventType().equals("RoundRobin")){
            String[] value=hostId.split(",");
            bookings.setHostIds((value[0]));
        }
        bookingsRepository.save(bookings);
        model.addAttribute("userId",userId);
        emailSender.sendHtmlMail(bookings.getEmail(),"Confirm Booking",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>"+bookings.getName()+",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>Your Slot got Confirmed.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>"+event.get().getEventType()+"</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>"+bookings.getName()+"</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>"+bookings.getEmail()+"</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>"+bookings.getTime()+"   "+bookings.getDate()+"</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>","anilbalaga2001@gmail.com");
        model.addAttribute("date",bookings.getDate());
        model.addAttribute("time",bookings.getTime());
        model.addAttribute("eventTime",bookings.getEvent().getDuration());
        return "comform";
    }

    public static List<String> splitTimeSlots(String startTimeStr, String endTimeStr, int slotDurationMinutes, int timeGapMinutes) {
        List<String> timeSlots = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalTime startTime = LocalTime.parse(startTimeStr, formatter);
        LocalTime endTime = LocalTime.parse(endTimeStr, formatter);

        while (startTime.plusMinutes(slotDurationMinutes).isBefore(endTime) || startTime.plusMinutes(slotDurationMinutes).equals(endTime)) {
            String timeSlot = startTime.format(formatter) + " - " + startTime.plusMinutes(slotDurationMinutes).format(formatter);
            timeSlots.add(timeSlot);
            startTime = startTime.plusMinutes(slotDurationMinutes).plusMinutes(timeGapMinutes);
        }

        if (!startTime.equals(endTime)) {
            String timeSlot = startTime.format(formatter) + " - " + endTime.format(formatter);
            timeSlots.add(timeSlot);
        }

        return timeSlots;
    }

    public String  getTime(String dayOfWeek,Schedule dateByTime){
        String timeByDay;
        if(dayOfWeek.equals("sunday")){
            timeByDay=dateByTime.getSunday();
        }else if(dayOfWeek.equals("monday")){
            timeByDay=dateByTime.getMonday();
        }else if(dayOfWeek.equals("tuesday")){
            timeByDay=dateByTime.getTuesday();
        }else if(dayOfWeek.equals("wednesday")){
            timeByDay=dateByTime.getWednesday();
        }else if(dayOfWeek.equals("thursday")){
            timeByDay=dateByTime.getThursday();
        }else if(dayOfWeek.equals("friday")){
            timeByDay=dateByTime.getFriday();
        }else if(dayOfWeek.equals("saturday")){
            timeByDay=dateByTime.getSaturday();
        }else{
            timeByDay="UnAvailable,";
        }
        return timeByDay;
    }

    public Map<String,String> getTimeSlots(String[] totalTime,
                                           int slotDurationMinutes,
                                           int timeGapMinutes,Date dateString,
                                           int eventId,int numberOfSlot){
        String startTimeStr =totalTime[0];
        String endTimeStr =totalTime[1];
        LinkedHashMap<String,String> timeSlotsForThymlef=new LinkedHashMap<>();
        List<String> timeSlots=splitTimeSlots(startTimeStr,endTimeStr,slotDurationMinutes,timeGapMinutes);
        List<String> checkInDatabase=bookingsRepository.getTimeSlots(dateString,eventId);
        for(String time:timeSlots){
            if( bookingsRepository.getTimeSlotCount(dateString,time,eventId) <=numberOfSlot ){
                int slots=numberOfSlot-bookingsRepository.findBySlotCount(eventId,time);
                timeSlotsForThymlef.put(time,"Available slots "+slots);
            }
        }
        return timeSlotsForThymlef;
    }


    @PostMapping ("/editProcess-date")
    public String slots(@RequestParam("eventId") int eventId,
                        @RequestParam(value = "bookingId",defaultValue = "",required = false)long bookingId,
                        @RequestParam("selectedDate") String date, Model model, RedirectAttributes redirectAttributes) throws ParseException, MessagingException, jakarta.mail.MessagingException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        Event event=eventsRepository.findById(eventId).get();
        if(event.getEventType().equals("Collective")){
            redirectAttributes.addAttribute("eventId",eventId);
            redirectAttributes.addAttribute("selectedDate",date);
            return "redirect:/collective-booking";
        }if(event.getEventType().equals("RoundRobin")){
            redirectAttributes.addAttribute("eventId",eventId);
            redirectAttributes.addAttribute("selectedDate",date);
            return "redirect:/RoundRobin-booking";
        }
        String eventType=event.getEventType();
        int scheduleId= Integer.parseInt(event.getScheduleId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        System.err.println(dayOfWeek);
        Optional<Schedule> schedule=scheduleRepository.findById(scheduleId);
        Date fromDate=event.getFromDate();
        Date toDate=event.getToDate();
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Schedule  dateByTime= scheduleRepository.findTimeByDay(scheduleId);
        String timeByDay=getTime(dayOfWeek,dateByTime);
        if(!timeByDay.equals("UnAvailable,") && timeByDay!=null){
            String[] totalTime= (timeByDay.substring(0,timeByDay.length()).split("-"));
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            int numberOfSlot=event.getNumberOfSlots();
            Map<String,String> timeSlotsForThymeleaf=getTimeSlots(totalTime, slotDurationMinutes, timeGapMinutes,
                    dateString,eventId,numberOfSlot);
            model.addAttribute("slotTimes",timeSlotsForThymeleaf);
        }else{
            model.addAttribute("message","No slots available on this day ");
        }
        Bookings bookings=bookingsRepository.findById(bookingId);
        System.out.println(bookings.getId()+" booking id");
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("booking",bookings);
        model.addAttribute("eventType",event.getEventType());
        emailSender.sendHtmlMail(bookings.getEmail(),"Rescheduled",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>"+bookings.getName()+",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>Trying to Rescheduled your event with the below details.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>"+event.getEventType()+"</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>"+bookings.getName()+"</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>"+bookings.getEmail()+"</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>"+bookings.getTime()+"   "+bookings.getDate()+"</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>","anilbalaga2001@gmail.com");
        return "calender";
    }

    @PostMapping ("/process-date")
    public String slots(@RequestParam("eventId") int eventId,
                        @RequestParam("selectedDate") String date, Model model, RedirectAttributes redirectAttributes) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        Event event=eventsRepository.findById(eventId).get();

        if(event.getEventType().equals("Collective")){

            redirectAttributes.addAttribute("eventId",eventId);
            redirectAttributes.addAttribute("selectedDate",date);
            return "redirect:/collective-booking";
        }if(event.getEventType().equals("RoundRobin")){

            redirectAttributes.addAttribute("eventId",eventId);
            redirectAttributes.addAttribute("selectedDate",date);
            return "redirect:/RoundRobin-booking";
        }
        String eventType=event.getEventType();
        int scheduleId= Integer.parseInt(event.getScheduleId());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        Optional<Schedule> schedule=scheduleRepository.findById(scheduleId);
        Date fromDate=event.getFromDate();
        Date toDate=event.getToDate();
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        Schedule  dateByTime= scheduleRepository.findTimeByDay(scheduleId);
        String timeByDay=getTime(dayOfWeek,dateByTime);
        System.err.println("time = "+timeByDay);
        if(!timeByDay.equals("UnAvailable,") && timeByDay.length()!=0){
            System.err.println("if");
            System.err.println(timeByDay.substring(0,timeByDay.length()).split("-"));
            String[] totalTime= (timeByDay.substring(0,timeByDay.length()).split("-"));
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            int numberOfSlot=event.getNumberOfSlots();
            Map<String,String> timeSlotsForThymeleaf=getTimeSlots(totalTime, slotDurationMinutes, timeGapMinutes,
                    dateString,eventId,numberOfSlot);
            model.addAttribute("slotTimes",timeSlotsForThymeleaf);
        }else{
            System.err.println("else is calling");
            model.addAttribute("message","No slots available on this day ");
        }
        if(event.getEventType().equals("Group")){
            model.addAttribute("group",1);
        }
        Bookings bookings=new Bookings();
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("fetch",2);
        model.addAttribute("booking",bookings);
        model.addAttribute("eventType",event.getEventType());
        return "calender";
    }

    @GetMapping("/calendar/{eventId}")
    public String showCalendar(@PathVariable("eventId") int id, Model model) {
        Optional<Event> optionalEvent=eventsRepository.findById(id);
        Event event=optionalEvent.get();
        Date fromDate=event.getFromDate();
        System.err.println(fromDate);
        Date toDate =event.getToDate();
        System.err.println(toDate);
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate(); // Events table range by id
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("eventId",id);
        model.addAttribute("eventType",event.getEventType());
        return "calender";
    }

    @RequestMapping(value = "/collective-booking",method = RequestMethod.GET)
    public String forBookingCollective(@RequestParam("eventId") int eventId,
                                       @RequestParam("selectedDate") String date, Model model) throws ParseException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        List<Schedule> listOfScheduleIds=new ArrayList<>();
        Event event=eventsRepository.findById(eventId).get();
        String[] scheduleIds=event.getScheduleId().split(",");
        Date fromDate=event.getFromDate();
        Date toDate=event.getToDate();
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i=0;i<scheduleIds.length;i++){
            Schedule schedule=scheduleRepository.findById(Integer.valueOf(scheduleIds[i])).get();
            listOfScheduleIds.add(schedule);
        }
        System.err.println(listOfScheduleIds);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        List<String> timeSlots=getTimeForCollective(dayOfWeek,listOfScheduleIds);
        System.err.println(timeSlots.size());
        System.err.println(timeSlots);
        if(!timeSlots.contains("") && !timeSlots.contains("UnAvailable,") ){
            System.err.println("collective if");
            Map<String,Integer> allSlots=new LinkedHashMap<>();
            Map<String,String> timeSlotsForThymeleaf=new LinkedHashMap<>();
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            int numberOfSlot=event.getNumberOfSlots();
            for (int i=0;i<timeSlots.size();i++){
                String[] totalTime= (timeSlots.get(i).substring(0,timeSlots.get(i).length()).split("-"));
                Map<String,Integer> slots=getTimeSlotsForCollective(totalTime,slotDurationMinutes,timeGapMinutes,dateString,eventId,numberOfSlot);
                for (Map.Entry<String, Integer> entry : slots.entrySet()){
                    allSlots.put(entry.getKey(),allSlots.getOrDefault(entry.getKey(),0)+1);
                }
            }
            for (Map.Entry<String, Integer> entry : allSlots.entrySet()){
                if(entry.getValue()==listOfScheduleIds.size()){
                    timeSlotsForThymeleaf.put(entry.getKey(),"Available slots "+1);
                }
            }
            model.addAttribute("slotTimes",timeSlotsForThymeleaf);
        }else {
            System.err.println("collective else");
            model.addAttribute("message","No slots available on this day ");
            model.addAttribute("condition",1);
        }
        Bookings bookings=new Bookings();
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("eventType",event.getEventType());
        model.addAttribute("booking",bookings);
        return "calender";
    }
    @RequestMapping("/editCollective-booking")
    public String forBookingCollective(@RequestParam("eventId") int eventId,
                                       @RequestParam(value = "bookingId") long bookingId,
                                       @RequestParam("selectedDate") String date, Model model) throws ParseException, MessagingException, jakarta.mail.MessagingException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        List<Schedule> listOfScheduleIds=new ArrayList<>();
        Event event=eventsRepository.findById(eventId).get();
        String[] scheduleIds=event.getScheduleId().split(",");
        Date fromDate=event.getFromDate();
        Date toDate=event.getToDate();
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i=0;i<scheduleIds.length;i++){
            Schedule schedule=scheduleRepository.findById(Integer.valueOf(scheduleIds[i])).get();
            listOfScheduleIds.add(schedule);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        List<String> timeSlots=getTimeForCollective(dayOfWeek,listOfScheduleIds);
        if(!timeSlots.contains(null) && !timeSlots.contains("UnAvailable,")){
            Map<String,Integer> allSlots=new LinkedHashMap<>();
            Map<String,String> timeSlotsForThymeleaf=new LinkedHashMap<>();
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            int numberOfSlot=event.getNumberOfSlots();
            for (int i=0;i<timeSlots.size();i++){
                String[] totalTime= (timeSlots.get(i).substring(0,timeSlots.get(i).length()).split("-"));
                Map<String,Integer> slots=getTimeSlotsForCollective(totalTime,slotDurationMinutes,timeGapMinutes,dateString,eventId,numberOfSlot);
                for (Map.Entry<String, Integer> entry : slots.entrySet()){
                    allSlots.put(entry.getKey(),allSlots.getOrDefault(entry.getKey(),0)+1);
                }
            }
            for (Map.Entry<String, Integer> entry : allSlots.entrySet()){
                if(entry.getValue()==listOfScheduleIds.size()){
                    timeSlotsForThymeleaf.put(entry.getKey(),"Available slots "+1);
                }
            }
            model.addAttribute("slotTimes",timeSlotsForThymeleaf);
        }else {
            model.addAttribute("message","No slots available on this day ");
            model.addAttribute("condition",1);
        }
        Bookings bookings=bookingsRepository.findById(bookingId);
        System.out.println(bookings.getId()+"  in reschedule");
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("eventType",event.getEventType());
        model.addAttribute("booking",bookings);
        emailSender.sendHtmlMail(bookings.getEmail(),"Rescheduled",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>"+bookings.getName()+",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>Trying to Rescheduled your event with the below details.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>"+event.getEventType()+"</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>"+bookings.getName()+"</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>"+bookings.getEmail()+"</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>"+bookings.getTime()+"   "+bookings.getDate()+"</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>","anilbalaga2001@gmail.com");
        return "calender";
    }
    public Map<UserInfo,String>   getTimeForCollectiveAndRoundRobin(String dayOfWeek,List<Schedule> dateByTime){
        Map<UserInfo,String> timeByDay=new LinkedHashMap();
        for(Schedule schedule:dateByTime){
            if(dayOfWeek.equals("sunday")){
                timeByDay.put(schedule.getUser(),schedule.getSunday());
            }else if(dayOfWeek.equals("monday")){
                timeByDay.put(schedule.getUser(),schedule.getMonday());
            }else if(dayOfWeek.equals("tuesday")){
                timeByDay.put(schedule.getUser(),schedule.getTuesday());
            }else if(dayOfWeek.equals("wednesday")){
                timeByDay.put(schedule.getUser(),schedule.getWednesday());
            }else if(dayOfWeek.equals("thursday")){
                timeByDay.put(schedule.getUser(),schedule.getThursday());
            }else if(dayOfWeek.equals("friday")){
                timeByDay.put(schedule.getUser(),schedule.getFriday());
            }else if(dayOfWeek.equals("saturday")){
                timeByDay.put(schedule.getUser(),schedule.getSaturday());
            }else{
                timeByDay.put(schedule.getUser(),"UnAvailable,");
            }
        }
        return timeByDay;
    }

    @RequestMapping("/editRoundRobin-booking")
    public String forBookingRoundRobin(@RequestParam("eventId") int eventId,
                                       @RequestParam("bookingId")long bookingId,
                                       @RequestParam("selectedDate") String date, Model model) throws ParseException, MessagingException, jakarta.mail.MessagingException {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        List<Schedule> listOfScheduleIds=new ArrayList<>();
        Event event=eventsRepository.findById(eventId).get();
        String[] scheduleIds=event.getScheduleId().split(",");
        Date fromDate=event.getFromDate();
        System.err.println(fromDate);
        Date toDate=event.getToDate();
        System.err.println(toDate);
        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i=0;i<scheduleIds.length;i++){
            Schedule schedule=scheduleRepository.findById(Integer.valueOf(scheduleIds[i])).get();
            listOfScheduleIds.add(schedule);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        Map<UserInfo,String> timeSlots=getTimeForCollectiveAndRoundRobin(dayOfWeek,listOfScheduleIds);
        System.err.println(timeSlots);
        int numberOfSlot=0;
        List<String> slots=new ArrayList<>();
        //Set<UserInfo> userId=event.getUserInfo().getOrganisationMembers();
        List<Integer> listOfOrganisation=new ArrayList<>();
        for(Map.Entry<UserInfo,String> time:timeSlots.entrySet()){
            if(time.getValue()!=null && !time.getValue().equals("UnAvailable,")){
                listOfOrganisation.add(time.getKey().getId());
                slots.add(time.getValue());
                numberOfSlot++;
            }
        }
        System.err.println("Organization = "+listOfOrganisation.size()+"    "+listOfOrganisation);
        System.err.println("Slots = "+slots.size() + "   "+slots);

        if(numberOfSlot!=0){
            Map<String,Integer> timeSlotsForThymeleaf=new LinkedHashMap<>();
            Map<String,Integer> finalTimeSlotsForThymeleaf=new TreeMap<>();
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            for(int i=0;i<listOfOrganisation.size();i++){
                if(!slots.get(i).equals("") && !slots.get(i).equals("UnAvailable,")) {
                    System.err.println(slots.get(i));
                    String[] totalSlots = (slots.get(i).substring(0, slots.get(i).length()).split("-"));
                    System.err.println(totalSlots[i]);
                    timeSlotsForThymeleaf = getTimeSlotsForRoundRobin(totalSlots, slotDurationMinutes, timeGapMinutes, dateString, eventId, listOfOrganisation.get(i));
                   for(Map.Entry<String,Integer> slot:timeSlotsForThymeleaf.entrySet()){
                       int checking=bookingsRepository.findSlotsByUserId(eventId,slot.getKey(), String.valueOf(slot.getValue()),dateString);
                       if(checking==0) {
                           finalTimeSlotsForThymeleaf.put(slot.getKey(), slot.getValue());
                       }
                   }
                }
            }
            System.err.println("Final = "+finalTimeSlotsForThymeleaf);
            model.addAttribute("slotTimes",finalTimeSlotsForThymeleaf);


        }else{
            model.addAttribute("message","No slots available on this day ");
            model.addAttribute("condition",1);
        }
        Bookings bookings = bookingsRepository.findById(bookingId);
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("eventType",event.getEventType());
        model.addAttribute("booking",bookings);
        emailSender.sendHtmlMail(bookings.getEmail(),"Rescheduled",
                "<html >\n" +
                        "<head>\n" +
                        "</head>\n" +
                        "<body><header style='margin-left:30%'><img src='https://assets.calendly.com/assets/frontend/media/logo-square-cd364a3c33976d32792a.png'></header>\n" +
                        "    <div style='margin-left:10%'>\n" +
                        "    <div style='border:1px solid white'>\n" +
                        "    <h4>hi <span>"+bookings.getName()+",</span></h4>\n" +
                        "    <h4 style='font-size:25px;'>Trying to Rescheduled your event with the below details.</h4>\n" +
                        "    <h2>Event Type :</h2>\n" +
                        "    <h4>"+event.getEventType()+"</h4>\n" +
                        "    <h2>Name :</h2>\n" +
                        "    <h4>"+bookings.getName()+"</h4>\n" +
                        "    <h2>Invite Email :</h2>\n" +
                        "    <h4>"+bookings.getEmail()+"</h4>\n" +
                        "    <h2>Event Date/Time :</h2>\n" +
                        "    <h4>"+bookings.getTime()+"  "+bookings.getDate()+"</h4>\n" +
                        "    </div>    \n" +
                        "</div>\n" +
                        "</body>\n" +
                        "</html>","anilbalaga2001@gmail.com");
        return "calender";
    }

    @RequestMapping("/RoundRobin-booking")
    public String forBookingRoundRobin(@RequestParam("eventId") int eventId,
                                       @RequestParam("selectedDate") String date, Model model) throws ParseException {

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateString = dateFormat.parse(date);
        List<Schedule> listOfScheduleIds=new ArrayList<>();
        Event event=eventsRepository.findById(eventId).get();
        String[] scheduleIds=event.getScheduleId().split(",");
        Date fromDate=event.getFromDate();

        Date toDate=event.getToDate();

        LocalDate startDate =  fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate endDate = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        for(int i=0;i<scheduleIds.length;i++){
            Schedule schedule=scheduleRepository.findById(Integer.valueOf(scheduleIds[i])).get();
            listOfScheduleIds.add(schedule);
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate localDate = LocalDate.parse(date, formatter);
        String dayOfWeek = localDate.getDayOfWeek().name().toLowerCase(Locale.ROOT);
        Map<UserInfo,String> timeSlots=getTimeForCollectiveAndRoundRobin(dayOfWeek,listOfScheduleIds);
        System.err.println("timeSlots   "+timeSlots);
        int numberOfSlot=0;
        List<String> slots=new ArrayList<>();
        List<Integer> listOfOrganisation=new ArrayList<>();
        for(Map.Entry<UserInfo,String> time:timeSlots.entrySet()){
            System.err.println(timeSlots);
            if(time.getValue().length()!=0 && !time.getValue().equals("UnAvailable,")){
                System.err.println(time.getKey()+" = "+time.getValue().length());
                listOfOrganisation.add(time.getKey().getId());
                slots.add(time.getValue());
                numberOfSlot++;
            }
        }
        if(numberOfSlot!=0){
            Map<String,Integer> timeSlotsForThymeleaf=new LinkedHashMap<>();
            Map<String,Integer> finalTimeSlotsForThymeleaf=new TreeMap<>();
            int slotDurationMinutes = event.getDuration();
            int timeGapMinutes = event.getBufferAfterEvent()+event.getBufferBeforeEvent();
            for(int i=0;i<listOfOrganisation.size();i++){
                if(!slots.get(i).equals(null) && !slots.get(i).equals("UnAvailable,")) {
                    System.err.println(slots.get(i));
                    String[] totalSlots = (slots.get(i).substring(0, slots.get(i).length()).split("-"));
                    System.err.println(totalSlots[i]);
                    timeSlotsForThymeleaf = getTimeSlotsForRoundRobin(totalSlots, slotDurationMinutes, timeGapMinutes, dateString, eventId, listOfOrganisation.get(i));
                    for(Map.Entry<String,Integer> slot:timeSlotsForThymeleaf.entrySet()){
                        int checking=bookingsRepository.findSlotsByUserId(eventId,slot.getKey(), String.valueOf(slot.getValue()),dateString);
                        if(checking==0) {
                            finalTimeSlotsForThymeleaf.put(slot.getKey(), slot.getValue());
                        }
                    }
                }
            }
            System.err.println("Final = "+finalTimeSlotsForThymeleaf);
            model.addAttribute("slotTimes",finalTimeSlotsForThymeleaf);


        }else{
            model.addAttribute("message","No slots available on this day ");
            model.addAttribute("condition",1);
        }
        Bookings bookings=new Bookings();
        model.addAttribute("value",1);
        model.addAttribute("startDate", startDate);
        model.addAttribute("endDate", endDate);
        model.addAttribute("date",date);
        model.addAttribute("eventId",eventId);
        model.addAttribute("eventType",event.getEventType());
        model.addAttribute("booking",bookings);
        return "calender";
    }



    public Map<String,Integer> getTimeSlotsForCollective(String[] totalTime,
                                                         int slotDurationMinutes,
                                                         int timeGapMinutes,Date dateString,
                                                         int eventId,int numberOfSlot){
        String startTimeStr =totalTime[0];
        String endTimeStr =totalTime[1];
        LinkedHashMap<String,Integer> timeSlotsForThymeleaf=new LinkedHashMap<>();
        List<String> timeSlots=splitTimeSlots(startTimeStr,endTimeStr,slotDurationMinutes,timeGapMinutes);
        List<String> checkInDatabase=bookingsRepository.getTimeSlots(dateString,eventId);
        for(String time:timeSlots){
            if( bookingsRepository.getTimeSlotCount(dateString,time,eventId) <=numberOfSlot ) {
                timeSlotsForThymeleaf.put(time, timeSlotsForThymeleaf.getOrDefault(time, 0) + 1);
            }
        }

        return timeSlotsForThymeleaf;
    }
    public Map<String,Integer> getTimeSlotsForRoundRobin(String[] totalTime,
                                                         int slotDurationMinutes,
                                                         int timeGapMinutes,Date dateString,
                                                         int eventId,int userId){
        String startTimeStr =totalTime[0];
        String endTimeStr =totalTime[1];
        LinkedHashMap<String,Integer> timeSlotsForThymeleaf=new LinkedHashMap<>();
        List<String> timeSlots=splitTimeSlots(startTimeStr,endTimeStr,slotDurationMinutes,timeGapMinutes);
      System.err.println(eventId);
        for(String time:timeSlots){
            int checking=bookingsRepository.findSlotsByUserId(eventId,time, String.valueOf(userId),dateString);
                    timeSlotsForThymeleaf.put(time,userId);
        }
        return timeSlotsForThymeleaf;
    }
    public List<String>   getTimeForCollective(String dayOfWeek,List<Schedule> dateByTime){
        List<String> timeByDay=new ArrayList<>();
        for(Schedule schedule:dateByTime){
            if(dayOfWeek.equals("sunday")){
                timeByDay.add(schedule.getSunday());
            }else if(dayOfWeek.equals("monday")){
                timeByDay.add(schedule.getMonday());
            }else if(dayOfWeek.equals("tuesday")){
                timeByDay.add(schedule.getTuesday());
            }else if(dayOfWeek.equals("wednesday")){
                timeByDay.add(schedule.getWednesday());
            }else if(dayOfWeek.equals("thursday")){
                timeByDay.add(schedule.getThursday());
            }else if(dayOfWeek.equals("friday")){
                timeByDay.add(schedule.getFriday());
            }else if(dayOfWeek.equals("saturday")){
                timeByDay.add(schedule.getSaturday());
            }else{
                timeByDay.add("UnAvailable,");
            }
        }
        return timeByDay;
    }
    @ExceptionHandler(value = Exception.class)
    public String ExceptionHandlers() {
        return "error";
    }


    //booking page redirection praveen by the link to the page
    @RequestMapping(value="/{email}/{eventName}", method = RequestMethod.GET)
    public String handleRequest(@PathVariable("email") String email,@PathVariable("eventName") String eventName,Model model,Authentication authentication) {

        Optional<UserInfo> userInfo=userInfoRepository.findByEmail(email+"@gmail.com");
        if(userInfo.isEmpty()){
            return "EmailOrEventNameNotPresent";
        }else{
            List<Event> events=userInfo.get().getEvents();
            for(Event event:events){
                if(event.getEventLink().contains(eventName)){
                    Long id=event.getId();
                    return "redirect:/calendar/"+id+"";
                }
            }
        }
        return "EmailOrEventNameNotPresent";
    }
    @RequestMapping("/myBookings")
    public String myBookings(Model model,Authentication authentication)
    {
        UserOAuth2 userOAuth2= (UserOAuth2) authentication.getPrincipal();
        String mail=userOAuth2.getMail();
        int userId=userInfoRepository.findByEmail(mail).get().getId();
        List<Bookings> bookings=bookingsRepository.findByUserEmail(mail);
        for(Bookings bookings1:bookings)
        {
            System.out.println(bookings1.getEventType());
        }
        System.out.println(bookings);
        model.addAttribute("bookings",bookings);
        model.addAttribute("userId",userId);
        return "myBooking";
    }
}