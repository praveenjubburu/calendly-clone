package com.mountblue.calendly.security;

import com.mountblue.calendly.Entity.UserInfo;
import com.mountblue.calendly.dao.UserInfoRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.Optional;


@Component
public class OAuth2LoginSuccessHandler extends SavedRequestAwareAuthenticationSuccessHandler {
    @Autowired
    private UserInfoRepository userService;
    @Autowired
    private UserInfo user;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                               Authentication authentication) throws ServletException, IOException, UsernameNotFoundException {

        UserOAuth2 userOAuth2 = (UserOAuth2) authentication.getPrincipal();

        Optional<UserInfo> userInTheDatabase=userService.findByEmail(userOAuth2.getMail());

        if (userInTheDatabase.isPresent()) {

            response.sendRedirect("/dashboard");

        }else{

            user.setId(0);
            user.setEmail(userOAuth2.getMail());
            user.setName(userOAuth2.getName());
            user.setRole("USER");
            user.setStatus("NotActive");
            userService.save(user);

            response.sendRedirect("/dashboard");

        }

    }
}
