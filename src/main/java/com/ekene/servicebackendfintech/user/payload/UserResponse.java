package com.ekene.servicebackendfintech.user.payload;

import com.ekene.servicebackendfintech.user.model.FintechUser;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserResponse {
    private Long id;
    private String email;
    private String phoneNumber;
    private String role;
    private LocalDateTime createdAt;

    public static UserResponse fromUser(FintechUser user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
