package com.main.TravelMate.profile.controller;

import com.main.TravelMate.profile.dto.ProfileRequestDto;
import com.main.TravelMate.profile.dto.ProfileResponseDto;
import com.main.TravelMate.profile.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profiles")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @PostMapping
    public ResponseEntity<Void> createOrUpdateProfile(@RequestBody ProfileRequestDto dto,
                                                      @AuthenticationPrincipal UserDetails userDetails) {
        profileService.createOrUpdateProfile(userDetails.getUsername(), dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{userId}")
    public ResponseEntity<ProfileResponseDto> getProfile(@PathVariable Long userId) {
        return ResponseEntity.ok(profileService.getProfile(userId));
    }
}
