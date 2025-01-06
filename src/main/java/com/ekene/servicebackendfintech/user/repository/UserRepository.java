package com.ekene.servicebackendfintech.user.repository;

import com.ekene.servicebackendfintech.user.model.FintechUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<FintechUser, Long> {
    Optional<FintechUser> findByUserIdIgnoreCase(String userid);
    Optional<FintechUser> findByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);
}
