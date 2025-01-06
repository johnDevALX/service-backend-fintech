package com.ekene.servicebackendfintech.user.api;


import com.ekene.servicebackendfintech.user.payload.AuthPayload;
import com.ekene.servicebackendfintech.user.payload.UserDto;
import com.ekene.servicebackendfintech.user.service.UserService;
import com.ekene.servicebackendfintech.utils.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/user/")
public class AuthController extends BaseController {
    private final UserService userService;

    @PostMapping("create")
    public ResponseEntity<?> createUser(@Valid @RequestBody UserDto userDto){
        return getAppResponse(HttpStatus.CREATED, "Successful", userService.createUser(userDto));
    }

    @PostMapping("authenticate")
    public ResponseEntity<?> authenticateUser(@Valid @RequestBody AuthPayload authPayload){
        return getAppResponse(HttpStatus.OK, "Authenticated", userService.authenticateUser(authPayload));
    }
}
