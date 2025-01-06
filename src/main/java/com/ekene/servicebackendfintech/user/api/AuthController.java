package com.ekene.onlinebookstore.api;


import com.ekene.onlinebookstore.user.AuthService;
import com.ekene.onlinebookstore.user.util.AuthPayload;
import com.ekene.onlinebookstore.user.util.UserDto;
import com.ekene.onlinebookstore.utils.BaseController;
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
    private final AuthService authService;

    @PostMapping("create")
    public ResponseEntity<?> createUser(@RequestBody UserDto userDto){
        return getAppResponse(HttpStatus.CREATED, "Successful", authService.createLmsUser(userDto));
    }

    @PostMapping("authenticate")
    public ResponseEntity<?> authenticateUser(@RequestBody AuthPayload authPayload){
        return getAppResponse(HttpStatus.OK, "Authenticated", authService.authenticateUser(authPayload));
    }
}
