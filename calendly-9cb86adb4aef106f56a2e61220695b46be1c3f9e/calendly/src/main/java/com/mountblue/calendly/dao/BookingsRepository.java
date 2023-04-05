package com.mountblue.calendly.dao;

import com.mountblue.calendly.Entity.Bookings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface BookingsRepository extends JpaRepository<Bookings,Integer> {

    public Bookings findById(Long id);

    public List<Bookings> findAllById(Long id);

    @Query("SELECT b FROM Bookings b join b.event e WHERE e.id=:id")
    public  List<Bookings> findByEvent_id(@Param("id")int id);

    @Query("select time from Bookings b Join b.event e where date = :date and e.id = :eventId")
    public List<String> getTimeSlots(@Param("date") Date date, @Param("eventId") long id);

    List<Bookings> findByEmail(String email);
    @Query("SELECT b FROM Bookings b WHERE b.date BETWEEN :fromDate AND :toDate")
    public List<Bookings> sortByDate(@Param("fromDate") Date fromDate, @Param("toDate") Date toDate);

    @Query("SELECT count(time) from Bookings b Join event e Where time = :time and e.id = :eventId and date = :date")
    public int getTimeSlotCount(@Param("date") Date date,@Param("time") String time,@Param("eventId") int id);

    @Query("SELECT b FROM Bookings b JOIN b.event e WHERE e.id = :eventId AND b.id = :bookingId")
    public Bookings findByEventAndUser(@Param("eventId")int eventId,@Param("bookingId")int bookingId);

    @Query("Select count(time) From Bookings b Join b.event e where e.id = :eventId and b.time = :time")
    public int findBySlotCount(@Param("eventId") int eventId,@Param("time") String time);

    @Query("Select count(time) From Bookings b Join b.event e where e.id = :eventId and b.time = :time and b.hostIds = :hostIds and b.date = :date ")
    public int findSlotsByUserId(@Param("eventId") int eventId, @Param("time") String time, @Param("hostIds") String userId,@Param("date") Date date);

    @Query("SELECT b from Bookings b where b.email=:email order by b.date")
    public List<Bookings> findByUserEmail(@Param("email") String email);


}