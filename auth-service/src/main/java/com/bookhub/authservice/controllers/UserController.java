package com.bookhub.authservice.controllers;

import com.bookhub.authservice.dtos.requests.UserRolePatchRequestDto;
import com.bookhub.authservice.security.GatewayUserDetails;
import com.bookhub.authservice.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth/users")
public class UserController {

    private final UserService userService;


    @PatchMapping
    @PreAuthorize("hasRole('INTERNAL')")
    public ResponseEntity<Void> updateUserRole(@AuthenticationPrincipal GatewayUserDetails userDetails,
                                               @RequestBody UserRolePatchRequestDto userRolePatchRequestDto){
        userService.updateUserRole(userDetails.getUserId(),userRolePatchRequestDto.role());
        return ResponseEntity.noContent().build();
    }

}
