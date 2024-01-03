package com.example.usermanagement;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface PasswordResetTokenRepository extends CrudRepository<PasswordResetToken, Long> {

    PasswordResetToken findByTokenValue(String tokenValue);

    @Transactional
    void deleteByTokenValue(String tokenValue);
}
