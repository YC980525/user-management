package com.example.usermanagement;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserManagementRepository extends CrudRepository<User, Long> {
    User findByUsername(String username);

    @Transactional
    void deleteByUsername(String username);

    boolean existsByUsername(String username);
}
