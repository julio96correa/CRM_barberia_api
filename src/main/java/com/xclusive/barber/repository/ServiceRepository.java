package com.xclusive.barber.repository;

import com.xclusive.barber.entity.Service;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceRepository extends JpaRepository<Service, Long> {
    List<Service> findAllByActiveTrue();
    boolean existsByName(String name);
}
