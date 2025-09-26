package com.flex.interpre.domain.auth.repository;

import com.flex.interpre.domain.auth.entity.RefreshToken;
import org.springframework.data.repository.CrudRepository;

public interface RefreshTokenRespository extends CrudRepository<RefreshToken, Long> {
}
