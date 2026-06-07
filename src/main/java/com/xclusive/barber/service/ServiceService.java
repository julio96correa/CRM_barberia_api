package com.xclusive.barber.service;

import com.xclusive.barber.dto.service.ServiceRequest;
import com.xclusive.barber.dto.service.ServiceResponse;
import com.xclusive.barber.entity.Service;
import com.xclusive.barber.exception.DuplicateResourceException;
import com.xclusive.barber.exception.ResourceNotFoundException;
import com.xclusive.barber.repository.ServiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@org.springframework.stereotype.Service
@RequiredArgsConstructor
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public List<ServiceResponse> getActiveServices() {
        return serviceRepository.findAllByActiveTrue().stream()
                .map(this::toResponse)
                .toList();
    }

    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public ServiceResponse createService(ServiceRequest request) {
        if (serviceRepository.existsByName(request.getName())) {
            throw new DuplicateResourceException("name: service already exists");
        }
        Service service = Service.builder()
                .name(request.getName())
                .description(request.getDescription())
                .pointsValue(request.getPointsValue())
                .active(true)
                .build();
        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public ServiceResponse updateService(Long id, ServiceRequest request) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        service.setName(request.getName());
        service.setDescription(request.getDescription());
        service.setPointsValue(request.getPointsValue());
        return toResponse(serviceRepository.save(service));
    }

    @Transactional
    public void deactivateService(Long id) {
        Service service = serviceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Service not found: " + id));
        service.setActive(false);
        serviceRepository.save(service);
    }

    private ServiceResponse toResponse(Service service) {
        return ServiceResponse.builder()
                .id(service.getId())
                .name(service.getName())
                .description(service.getDescription())
                .pointsValue(service.getPointsValue())
                .active(service.getActive())
                .build();
    }
}
