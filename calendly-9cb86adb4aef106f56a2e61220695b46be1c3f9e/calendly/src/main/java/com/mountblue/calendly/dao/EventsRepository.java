package com.mountblue.calendly.dao;

import com.mountblue.calendly.Entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface EventsRepository extends JpaRepository<Event, Integer> {
    public Event findById(Long eventId);

    @Query("SELECT event FROM Event event JOIN event.userInfo user where user.id= :userId ")
    public List<Event> findEventsByUserId(@Param("userId") Long id);
    public List<Event> findByEventNameContainingIgnoreCase(String word);
}
