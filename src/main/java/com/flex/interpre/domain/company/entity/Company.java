package com.flex.interpre.domain.user.entity;

import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "companies")
public class Company {
    @Id
    @Getter(AccessLevel.NONE)
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "business_number", length = 50, unique = true)
    private String businessNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(length = 255)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;


    // 기업 정보 수정 메서드
    public void update(UpdateMyCompanyInfo request) {
        this.companyName = request.companyName();
        this.address = request.address();
        this.website = request.website();
        this.description = request.description();
        this.logoUrl = request.logoUrl();
    }
}
