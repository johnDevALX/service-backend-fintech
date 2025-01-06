package com.ekene.servicebackendfintech.user.payload;

import com.ekene.servicebackendfintech.constraints.annotation.EmailValidator;
import com.ekene.servicebackendfintech.constraints.annotation.PasswordValidator;
import com.ekene.servicebackendfintech.constraints.annotation.PhoneNumberValidator;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {
    @NotNull(message = "Email cannot be null")
    @EmailValidator(message = "Invalid email format, e.g: example@him.ai")
    private String email;

    @NotNull(message = "Phone number cannot be null")
    @PhoneNumberValidator(message = "Phone number must be 11 digits and start with '0'")
    private String phoneNumber;

    @NotNull(message = "Password cannot be null")
    @PasswordValidator(message = "Password must contain at least 8 characters, including uppercase, lowercase, and a number")
    private String password;

    @NotNull
    private String role;
}
