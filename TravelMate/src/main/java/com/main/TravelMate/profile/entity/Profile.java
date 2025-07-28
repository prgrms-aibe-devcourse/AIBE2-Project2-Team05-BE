package com.main.TravelMate.profile.entity;



import com.main.TravelMate.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
public class Profile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //프로필 id

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // 접속한 유저 id

    private String profileImage; // 프로필 사진 url
    private Integer age; // 나이
    private String gender; // 성별
    private String email; // 이메일
    private String phoneNumber; // 전화번호

    private String preferredDestinations;   //  선호 여행지 다중 선택 - 콤마 구분
    private String preferredTravelStyle;    //  선호 여행 스타일다중 선택 - 콤마 구분
    private String interests;               // 관심사 다중 선택 - 콤마 구분
    private String accommodationType; // 선호하는 숙박 유형
    private String budgetRange;// 예산 범위

    @Lob
    private String aboutMe; // 나에 대한 소개

    @Lob
    private String travelHistory; // 여행 경험

    @Lob
    private String languageSkills; // 언어 능력

    @CreationTimestamp
    private LocalDateTime createdAt;

    public Profile(User user, String profileImage, Integer age, String gender, String email, String phoneNumber,
                   String preferredDestinations, String preferredTravelStyle, String interests,
                   String accommodationType, String budgetRange, String aboutMe, String travelHistory,
                   String languageSkills) {
        this.user = user;
        this.profileImage = profileImage;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.preferredDestinations = preferredDestinations;
        this.preferredTravelStyle = preferredTravelStyle;
        this.interests = interests;
        this.accommodationType = accommodationType;
        this.budgetRange = budgetRange;
        this.aboutMe = aboutMe;
        this.travelHistory = travelHistory;
        this.languageSkills = languageSkills;
    }

    public void update(String profileImage, Integer age, String gender, String email, String phoneNumber,
                       String preferredDestinations, String preferredTravelStyle, String interests,
                       String accommodationType, String budgetRange, String aboutMe, String travelHistory,
                       String languageSkills) {
        this.profileImage = profileImage;
        this.age = age;
        this.gender = gender;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.preferredDestinations = preferredDestinations;
        this.preferredTravelStyle = preferredTravelStyle;
        this.interests = interests;
        this.accommodationType = accommodationType;
        this.budgetRange = budgetRange;
        this.aboutMe = aboutMe;
        this.travelHistory = travelHistory;
        this.languageSkills = languageSkills;
    }
}

