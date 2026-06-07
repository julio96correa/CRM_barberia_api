package com.xclusive.barber.repository;

import com.xclusive.barber.entity.ClientProfile;
import com.xclusive.barber.enums.ClientTier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ClientProfileRepository extends JpaRepository<ClientProfile, Long> {
    Optional<ClientProfile> findByUserId(Long userId);
    boolean existsByPhone(String phone);
    Optional<ClientProfile> findByPhone(String phone);

    long countByTier(ClientTier tier);

    @Query("SELECT COUNT(c) FROM ClientProfile c WHERE c.user.createdAt >= :from")
    long countNewSince(@Param("from") LocalDateTime from);

    @Query("SELECT c FROM ClientProfile c WHERE c.lastCompletedAt < :cutoff OR c.lastCompletedAt IS NULL")
    List<ClientProfile> findInactiveSince(@Param("cutoff") LocalDateTime cutoff);
}
