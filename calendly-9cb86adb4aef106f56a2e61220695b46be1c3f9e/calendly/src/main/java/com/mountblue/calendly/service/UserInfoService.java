package com.mountblue.calendly.service;

import com.mountblue.calendly.Entity.UserInfo;

import java.util.List;
import java.util.Optional;

public interface UserInfoService {

    public List<UserInfo> findAll();

    public List<UserInfo> findByNameContaining(String word);

    public Optional<UserInfo> findById(int parseInt);

    public UserInfo findByEmail(String s);
}
