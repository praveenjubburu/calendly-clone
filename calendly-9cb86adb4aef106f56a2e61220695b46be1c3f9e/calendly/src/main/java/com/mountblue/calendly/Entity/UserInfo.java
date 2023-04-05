package com.mountblue.calendly.Entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.apache.catalina.User;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Component
@Scope("prototype")
public class UserInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;
    private String name;
    private String message;
    private String country;
    private String timeZone;

    private String organizationName;

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }

    public List<ActivityLog> getActivityLogs() {
        return activityLogs;
    }

    public void setActivityLogs(List<ActivityLog> activityLogs) {
        this.activityLogs = activityLogs;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public UserInfo getOwnerOfOrganisation() {
        return ownerOfOrganisation;
    }

    public void setOwnerOfOrganisation(UserInfo ownerOfOrganisation) {
        this.ownerOfOrganisation = ownerOfOrganisation;
    }

    public Set<UserInfo> getOrganisationMembers() {
        return organisationMembers;
    }

    public void setOrganisationMembers(Set<UserInfo> organisationMembers) {
        this.organisationMembers = organisationMembers;
    }

    @Column(unique = true)
    private String email;


    private String role;

    private String status;

    @OneToMany(mappedBy = "userInfo",cascade = CascadeType.ALL)
    List<ActivityLog> activityLogs=new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ownerOfOrganisation")
    private UserInfo ownerOfOrganisation;

    @OneToMany(mappedBy = "ownerOfOrganisation", cascade = CascadeType.ALL)
    private Set<UserInfo> organisationMembers = new HashSet<UserInfo>();

    @JsonManagedReference
    public List<Schedule> getSchedules() {
        return schedules;
    }

    public void setSchedules(List<Schedule> schedules) {
        this.schedules = schedules;
    }

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userInfo")
    private List<Event> events;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "userInfo", fetch = FetchType.EAGER)
    private List<Schedule> schedules = new ArrayList<>();

    public List<Event> getEvents() {
        return events;
    }

    public void setEvents(List<Event> events) {
        this.events = events;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}