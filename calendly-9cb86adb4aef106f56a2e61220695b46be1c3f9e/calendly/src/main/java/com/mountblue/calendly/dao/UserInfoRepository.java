package com.mountblue.calendly.dao;

import com.mountblue.calendly.Entity.Event;
import com.mountblue.calendly.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    @Query("Select u from UserInfo u where email = :email")
    public Optional<UserInfo> findByEmail(@Param("email") String email);

    public List<UserInfo> findByNameContainingIgnoreCase(String word);


}
