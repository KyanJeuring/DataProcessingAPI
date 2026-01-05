package com.fleetmaster.repositories;

import com.fleetmaster.entities.Info;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InfoRepository extends JpaRepository<Info, Long> {
}
