package com.usman.resourcebooking.repository;

import com.usman.resourcebooking.model.Resource;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceRepository extends JpaRepository<Resource, Long> {
}