package org.example.expert.domain.user.controller;

import lombok.RequiredArgsConstructor;
import org.example.expert.domain.common.annotation.LogAdmin;
import org.example.expert.domain.user.dto.request.UserRoleChangeRequest;
import org.example.expert.domain.user.service.UserAdminService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserAdminController {

    private final UserAdminService userAdminService;

    @LogAdmin(comment = "Admin change userRole")
    @PatchMapping("/admin/users/{userId}")
    // AOP 를 이용해서 response 까지 로깅을 잘 하는지 확인하기 위해서
    // 기존 void -> ResponseEntity<String> 으로 변경
    public ResponseEntity<String> changeUserRole(@PathVariable long userId, @RequestBody UserRoleChangeRequest userRoleChangeRequest) {
        userAdminService.changeUserRole(userId, userRoleChangeRequest);
        return new ResponseEntity<>(
                "changed id: " + userId
                + ", changed role: " + userRoleChangeRequest.getRole(),
                HttpStatus.OK
        );
    }
}
