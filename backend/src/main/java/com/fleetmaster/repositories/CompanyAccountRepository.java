package com.fleetmaster.repositories;

import com.fleetmaster.entities.CompanyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyAccountRepository extends JpaRepository<CompanyAccount, Long> {
    Optional<CompanyAccount> findByEmail(String email);
}
