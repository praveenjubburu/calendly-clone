package com.mountblue.calendly.dao;

import com.mountblue.calendly.Entity.Schedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ScheduleRepository extends JpaRepository<Schedule,Integer> {

    @Query("SELECT s from Schedule s where id = :id ")
    public Schedule findTimeByDay(@Param("id") int id);

    public Schedule findByName(String defaultSchedule);
    @Query("Select s from Schedule s where id in :id")
    public List<Schedule> findTimeByDayForCollective(@Param("id") List<Integer> id);
}
