package com.example.usermanagement;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;

@Service
public class DatabaseInitializer {

    @Autowired
    UserManagementRepository userManagementRepository;

    @Autowired
    PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        passwordResetTokenRepository.deleteAll();
        userManagementRepository.deleteAll();

        User defaultAdmin = new User();
        defaultAdmin.setUsername("defaultAdmin");
        defaultAdmin.setPassword(passwordEncoder.encode("defaultAdminPassword"));
        defaultAdmin.setEmail("defaultAdmin@domain.com");
        defaultAdmin.setEnabled(true);
        defaultAdmin.setRoles("ADMIN", "USER");
        userManagementRepository.save(defaultAdmin);

        User defaultUser = new User();
        defaultUser.setUsername("defaultUser");
        defaultUser.setPassword(passwordEncoder.encode("defaultUserPassword"));
        defaultUser.setEmail("defaultUser@domain.com");
        defaultUser.setEnabled(true);
        defaultUser.setRoles("USER");

        userManagementRepository.save(defaultUser);
    }
}
