package com.mountblue.calendly.service;

import com.mountblue.calendly.Entity.UserInfo;
import com.mountblue.calendly.dao.UserInfoRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@AllArgsConstructor
@NoArgsConstructor
@Service
public class UserInfoServiceImplementation implements UserInfoService{
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Override
    public List<UserInfo> findAll() {
        return userInfoRepository.findAll();
    }

    @Override
    public List<UserInfo> findByNameContaining(String word) {
        return userInfoRepository.findByNameContainingIgnoreCase(word);
    }

    @Override
    public Optional<UserInfo> findById(int parseInt) {
        return userInfoRepository.findById(parseInt);
    }

    @Override
    public UserInfo findByEmail(String email) {
        return userInfoRepository.findByEmail(email).get();
    }
}
