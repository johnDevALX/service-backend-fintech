package com.ekene.onlinebookstore.auth;


import com.ekene.onlinebookstore.user.model.ObsUser;
import com.ekene.onlinebookstore.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CustomUserService implements UserDetailsService {
    private final UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<ObsUser> user = userRepository.findObsUserByEmailIgnoreCase(username);
        return user.map(CustomUser::new).orElseThrow(() -> new UsernameNotFoundException("User not found " + username));
    }
}
