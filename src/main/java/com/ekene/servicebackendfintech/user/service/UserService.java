package com.ekene.servicebackendfintech.user.service;


import com.ekene.servicebackendfintech.auth.JwtUtil;
import com.ekene.servicebackendfintech.user.model.FintechUser;
import com.ekene.servicebackendfintech.user.payload.AuthPayload;
import com.ekene.servicebackendfintech.user.payload.ReturnObj;
import com.ekene.servicebackendfintech.user.payload.UserDto;
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
public class UserService {
    private static final String PREFIX = "FTH";
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public ReturnObj createUser (UserDto userDto){
        FintechUser.FintechUserBuilder builder = FintechUser.builder();
        builder.userId(generateUserId());
        builder.phoneNumber(userDto.getPhoneNumber());
        builder.email(userDto.getEmail());
        builder.password(passwordEncoder.encode(userDto.getPassword()));
        builder.role(userDto.getRole());

        FintechUser save = userRepository.save(builder.build());
        return extract(save.getEmail(), save.getUserId(), "");
    }

    public ReturnObj authenticateUser(AuthPayload authPayload){
        Authentication authentication;
        authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authPayload.getEmail(),
                        authPayload.getPassword()
                )
        );
        var user = userRepository.findByEmailIgnoreCase(authPayload.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("User not found!"));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return extract(user.getEmail(), "", getToken(user.getEmail()));
    }

    @Transactional(readOnly = true)
    public FintechUser getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
    }

    private String getToken(String email){
        return jwtUtil.generateToken(email);
    }
    private ReturnObj extract(String email, String userId, String token){
        return new ReturnObj(email, userId, token);
    }

    private String generateUserId() {
        long timestamp = System.currentTimeMillis();
        String numericPart = String.valueOf(timestamp).substring(5);
        return PREFIX + numericPart;
    }
}
