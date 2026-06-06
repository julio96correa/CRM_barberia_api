package com.xclusive.barber.repository;

import com.xclusive.barber.entity.BarberProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BarberProfileRepository extends JpaRepository<BarberProfile, Long> {
    Optional<BarberProfile> findByUserId(Long userId);
}
