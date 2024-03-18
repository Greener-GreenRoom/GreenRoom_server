package com.greenroom.server.api.domain.greenroom.controller;

import com.greenroom.server.api.domain.greenroom.service.ItemService;
import com.greenroom.server.api.utils.ApiResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @GetMapping("/items")
    public ResponseEntity<ApiResponse> getItemList(@AuthenticationPrincipal UserDetails userDetails){

        return ResponseEntity.ok(ApiResponse.success(itemService.getItemList(userDetails.getUsername())));
    }
}
