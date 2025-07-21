package com.main.TravelMate.profile.service;



import com.main.TravelMate.profile.dto.ProfileRequestDto;
import com.main.TravelMate.profile.dto.ProfileResponseDto;
import com.main.TravelMate.profile.entity.Profile;
import com.main.TravelMate.profile.repository.ProfileRepository;
import com.main.TravelMate.user.entity.User;
import com.main.TravelMate.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    public void createOrUpdateProfile(String email, ProfileRequestDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자 없음"));

        Optional<Profile> optionalProfile = profileRepository.findByUser(user);

        if (optionalProfile.isPresent()) {
            Profile profile = optionalProfile.get();
            profile.update(
                    dto.getProfileImage(),
                    dto.getAge(),
                    dto.getGender(),
                    dto.getEmail(),
                    dto.getPhoneNumber(),
                    dto.getPreferredDestinations(),
                    dto.getPreferredTravelStyle(),
                    dto.getInterests(),
                    dto.getAccommodationType(),
                    dto.getBudgetRange(),
                    dto.getAboutMe(),
                    dto.getTravelHistory(),
                    dto.getLanguageSkills()
            );
        } else {
            Profile newProfile = new Profile(
                    user,
                    dto.getProfileImage(),
                    dto.getAge(),
                    dto.getGender(),
                    dto.getEmail(),
                    dto.getPhoneNumber(),
                    dto.getPreferredDestinations(),
                    dto.getPreferredTravelStyle(),
                    dto.getInterests(),
                    dto.getAccommodationType(),
                    dto.getBudgetRange(),
                    dto.getAboutMe(),
                    dto.getTravelHistory(),
                    dto.getLanguageSkills()
            );
            profileRepository.save(newProfile);
        }
    }

    public ProfileResponseDto getProfile(Long userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("프로필 없음"));

        return new ProfileResponseDto(
                profile.getUser().getNickname(),
                profile.getProfileImage(),
                profile.getAge(),
                profile.getGender(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getPreferredDestinations(),
                profile.getPreferredTravelStyle(),
                profile.getInterests(),
                profile.getAccommodationType(),
                profile.getBudgetRange(),
                profile.getAboutMe(),
                profile.getTravelHistory(),
                profile.getLanguageSkills()
        );
    }
}
