package com.fleetmaster.repositories;

import com.fleetmaster.entities.ApiAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ApiAccountRepository extends JpaRepository<ApiAccount, Long> {
    Optional<ApiAccount> findByUsername(String username);
}
