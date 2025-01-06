package com.ekene.servicebackendfintech.user.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnObj {
    private String email;
    private String userId;
    private String token;
}
