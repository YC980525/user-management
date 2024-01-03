package com.example.usermanagement;

import org.springframework.data.repository.CrudRepository;
import org.springframework.transaction.annotation.Transactional;

public interface UserManagementRepository extends CrudRepository<User, String> {
    User findByUsername(String username);

    User findByEmail(String email);

    @Transactional
    void deleteByUsername(String username);

    boolean existsByUsername(String username);

}
