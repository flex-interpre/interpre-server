package com.flex.interpre.domain.user.repository;

import com.flex.interpre.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface CompanyRepository extends JpaRepository<Company, UUID> {
    @Query("SELECT c FROM Company c JOIN FETCH c.user u WHERE c.user.id = :id")
    Optional<Company> findByUserIdWithUser(UUID id);
}
