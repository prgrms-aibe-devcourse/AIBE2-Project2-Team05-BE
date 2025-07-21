package com.main.TravelMate.profile.dto;



import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ProfileRequestDto {
    private String profileImage;
    private Integer age;
    private String gender;
    private String email;
    private String phoneNumber;
    private String preferredDestinations;
    private String preferredTravelStyle;
    private String interests;
    private String accommodationType;
    private String budgetRange;
    private String aboutMe;
    private String travelHistory;
    private String languageSkills;
}
