package com.flex.interpre.domain.company.entity;

import com.flex.interpre.domain.recruitment.entity.Recruitment;
import com.flex.interpre.domain.user.dto.request.UpdateMyCompanyInfo;
import com.flex.interpre.global.constant.Role;
import com.flex.interpre.global.security.authentication.AccountPrincipal;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "companies", indexes = {
        @Index(name = "idx_email", columnList = "email"),
        @Index(name = "idx_google_id", columnList = "google_id")
})
public class Company implements AccountPrincipal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false, columnDefinition = "uuid")
    private UUID id;

    // 공통 필드

    @Column(nullable = false, length = 255, unique = true)
    private String email;

    @Column(name = "google_id", length = 255, unique = true) // 다른 소셜 로그인 고려 nullable
    private String googleId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "is_approved", nullable = false)
    @Builder.Default
    private boolean approved = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;


    // 기업 필드

    @Column(name = "company_name", length = 200)
    private String companyName;

    @Column(name = "business_number", length = 50, unique = true)
    private String businessNumber;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(columnDefinition = "TEXT")
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Recruitment> recruitments = new HashSet<>();


    // 기업 메서드

    // 인증용 role 반환
    public Role getRole() {
        return Role.COMPANY;
    }

    // 관리자의 기업 계정 승인용 메서드
    public void approve() {
        this.approved = true;
    }

    // 기업 정보 수정 메서드
    public void update(UpdateMyCompanyInfo request) {
        this.companyName = request.companyName();
        this.address = request.address();
        this.website = request.website();
        this.description = request.description();
        this.logoUrl = request.logoUrl();
    }
}
