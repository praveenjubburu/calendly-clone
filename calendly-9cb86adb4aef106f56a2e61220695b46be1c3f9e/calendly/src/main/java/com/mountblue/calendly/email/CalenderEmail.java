package com.mountblue.calendly.email;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Properties;
import javax.activation.DataHandler;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;

@NoArgsConstructor
@AllArgsConstructor
@Service
public class CalenderEmail {
    public String getToEmail() {
        return toEmail;
    }

    public void setToEmail(String toEmail) {
        this.toEmail = toEmail;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getFromEmail() {
        return fromEmail;
    }

    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }

    public String getEmailBody() {
        return emailBody;
    }

    public void setEmailBody(String emailBody) {
        this.emailBody = emailBody;
    }

    private String fromEmail;

    private String toEmail;

    private String subject;

    private String startDate;

    private String endDate;

    private String startTime;
    private String endTime;

    private String emailBody;

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    private String location;


    public void sendEmail(){

        try {

            final String username = "calendly.aap@gmail.com";
            final String password = "mahbqpcfbcofasrw";

            String from = "calendly.aap@gmail.com";
            print(from);
            String to = this.toEmail;
            print(to);
            String subject = this.subject;
            print(subject);
            String startDate = convertDate(this.startDate); // Date Formate: YYYYMMDD
            print(startDate);
            String endDate =  convertDate(this.endDate); // Date Formate: YYYYMMDD
            print(endDate);
            String startTime =convertTime(this.startTime); // Time Formate: HHMM
            print(startTime);
            String endTime = convertTime(this.endTime); // Time Formate: HHMM
            print(endTime);
            String emailBody = this.emailBody;
            print(emailBody);

            String location=this.location;

            System.err.println("Start Time: " + startDate + "T" + startTime + "00Z");
            System.err.println("End Time: " + endDate + "T" + endTime + "00Z");


            Properties prop = new Properties();
            prop.put("mail.smtp.auth", "true");
            prop.put("mail.smtp.starttls.enable", "true");
            prop.put("mail.smtp.host", "smtp.gmail.com");
            prop.put("mail.smtp.port", "25");

            Session session = Session.getDefaultInstance(prop,  new Authenticator() {
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(username, password);
                }
            });

            MimeMessage message = new MimeMessage(session);
            message.addHeaderLine("method=REQUEST");
            message.addHeaderLine("charset=UTF-8");
            message.addHeaderLine("component=VEVENT");
            message.setFrom(new InternetAddress(from, "New Google Calendar Event"));
            message.addRecipient(Message.RecipientType.TO, new InternetAddress(to));
            message.setSubject(subject);

            StringBuffer sb = new StringBuffer();

            StringBuffer buffer = sb.append("BEGIN:VCALENDAR\n" +
                    "PRODID:-//Microsoft Corporation//Outlook 9.0 MIMEDIR//EN\n" +
                    "VERSION:2.0\n" +
                    "METHOD:REQUEST\n" +
                    "BEGIN:VEVENT\n" +
                    "ATTENDEE;ROLE=REQ-PARTICIPANT;RSVP=TRUE:MAILTO:"+ to +"\n" +
                    "DTSTART:"+ startDate +"T"+ startTime +"00Z\n" +
                    "DTEND:"+ endDate +"T"+ endTime +"00Z\n" +
                    "LOCATION:"+location+"\n" +
                    "TRANSP:OPAQUE\n" +
                    "SEQUENCE:0\n" +
                    "UID:040000008200E00074C5B7101A82E00800000000002FF466CE3AC5010000000000000000100\n" +
                    " 000004377FE5C37984842BF9440448399EB02\n" +
                    "CATEGORIES:Meeting\n" +
                    "DESCRIPTION:"+ emailBody +"\n\n" +
                    "SUMMARY:Event Date\n" +
                    "PRIORITY:5\n" +
                    "CLASS:PUBLIC\n" +
                    "BEGIN:VALARM\n" +
                    "TRIGGER:PT1440M\n" +
                    "ACTION:DISPLAY\n" +
                    "DESCRIPTION:Reminder\n" +
                    "END:VALARM\n" +
                    "END:VEVENT\n" +
                    "END:VCALENDAR");

            // Create the message part
            BodyPart messageBodyPart = new MimeBodyPart();

            // Fill the message
            messageBodyPart.setHeader("Content-Class", "urn:content-classes:calendarmessage");
            messageBodyPart.setHeader("Content-ID", "calendar_message");
            messageBodyPart.setDataHandler(new DataHandler(
                    new ByteArrayDataSource(buffer.toString(), "text/calendar")));// very important

            // Create a Multipart
            Multipart multipart = new MimeMultipart();

            // Add part one
            multipart.addBodyPart(messageBodyPart);

            // Put parts in message
            message.setContent(multipart);

            // send message
            Transport.send(message);

        } catch (MessagingException me) {
            me.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    private String convertTime(String time) {
        String convertedTime="";
        for(int index=0;index<time.length();index++){
            if(time.charAt(index)==':'){
                continue;
            }
            convertedTime+=time.charAt(index);
        }
        return convertedTime;

    }

    private String convertDate(String date) {
        String convertedDate="";
        System.out.println("calender function "+date);
        for(int index=0;index<date.length();index++){
            if(date.charAt(index)=='-'){
                continue;
            }
            convertedDate+=date.charAt(index);
        }
        return convertedDate;
    }

    private void print(String s){
        System.out.println(s);
    }

}