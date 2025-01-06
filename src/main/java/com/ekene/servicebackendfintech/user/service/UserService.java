package com.ekene.servicebackendfintech.user.service;


import com.ekene.servicebackendfintech.auth.JwtUtil;
import com.ekene.servicebackendfintech.user.model.FintechUser;
import com.ekene.servicebackendfintech.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ReturnObj createLmsUser (UserDto userDto){
        ObsUser.ObsUserBuilder builder = ObsUser.builder();
        builder.email(userDto.getEmail());
        builder.password(passwordEncoder.encode(userDto.getPassword()));
        builder.role(userDto.getRole());

        userRepository.save(builder.build());
        return extract(userDto.getEmail(), "");
    }

    public ReturnObj authenticateUser(AuthPayload authPayload){
        Authentication authentication;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authPayload.getEmail(),
                        authPayload.getPassword()
                )
        );
        var user = userRepository.findObsUserByEmailIgnoreCase(authPayload.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return extract(user.getEmail(), getToken(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public FintechUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    public void deleteUser(Long id) {
        FintechUser user = getUserById(id);
        log.info("Deleting user: {}", user.getEmail());
        userRepository.delete(user);
    }

    private String getToken(String email){
        return jwtUtil.generateToken(email);
    }
    private ReturnObj extract(String email, String token){
        return new ReturnObj(email, token);
    }

}
