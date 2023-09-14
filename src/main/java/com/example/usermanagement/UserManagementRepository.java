package com.example.usermanagement;

import org.springframework.data.repository.CrudRepository;

public interface UserManagementRepository extends CrudRepository<User, Long> {

}
