package com.cantomiletea.chavez.auth;

import com.cantomiletea.chavez.user.UserInfoRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserInfoDetailsService implements UserDetailsService {

    private final UserInfoRepo userInfoRepo;

    /**
     * Attempts to retrieve the <code>UserDetails</code> associated with an email/username.
     * @param username The email/username of the account
     * @return The <code>UserDetails</code> associated with the account
     * @throws UsernameNotFoundException when the email/username is not registered with an account
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userInfoRepo
                .findByEmailOrUsername(username, username)
                .map(UserInfoDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("Username/email not found in DB"));
    }
}
