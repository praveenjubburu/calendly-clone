package com.mountblue.calendly.dao;

import com.mountblue.calendly.Entity.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityLogRepository extends JpaRepository<ActivityLog,Integer> {

}
