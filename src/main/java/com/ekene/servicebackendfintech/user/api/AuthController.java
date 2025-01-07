package com.ekene.servicebackendfintech.user.api;


import com.ekene.servicebackendfintech.user.payload.AuthPayload;
import com.ekene.servicebackendfintech.user.payload.UserDto;
import com.ekene.servicebackendfintech.user.service.UserService;
import com.ekene.servicebackendfintech.utils.BaseController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    @GetMapping("details")
    public ResponseEntity<?> getUser(@Valid @RequestParam String userEmail){
        return getAppResponse(HttpStatus.OK, "Retrieved", userService.getUserByEmail(userEmail));
    }

    @DeleteMapping("delete")
    public ResponseEntity<?> deleteUser(@Valid @RequestParam String userEmail){
        return getAppResponse(HttpStatus.OK, "Deleted", userService.getUserByEmail(userEmail));
    }
}
