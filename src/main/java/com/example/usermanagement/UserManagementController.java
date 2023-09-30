package com.example.usermanagement;

import java.net.URI;
import java.util.HashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/home")
@Slf4j
public class UserManagementController {

    @Autowired
    private UserManagementRepository userManagementRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping(path = "/admin/all-users")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userManagementRepository.findAll();
    }

    @GetMapping("/{username}/profile")
    public ResponseEntity<User> getUserProfile(
        @PathVariable String username,
        @AuthenticationPrincipal UserDetails userDetails) {

        if (!username.equals(userDetails.getUsername())) {
            return ResponseEntity.notFound().build();
        }

        User user = userManagementRepository.findByUsername(username);

        if (user != null) {
            return ResponseEntity.ok(user);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> logIn(
        @AuthenticationPrincipal UserDetails userDetails,
        UriComponentsBuilder uriComponentsBuilder) {
        var uri = uriComponentsBuilder
            .path("/home/{username}/profile")
            .buildAndExpand(userDetails.getUsername())
            .toUri();

        return ResponseEntity.ok().location(uri).build();
    }

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(
        @RequestBody HashMap<String, String> requestBody,
        UriComponentsBuilder uriComponentsBuilder) {
        if (!requestBody.containsKey("username") || (!requestBody.containsKey("password"))) {
            return ResponseEntity.badRequest().body("Username or password not provided.");
        }

        if (userManagementRepository
            .existsByUsername(requestBody.get("username"))) {
            return ResponseEntity.badRequest().body("Username already exists.");
        }

        var user = new User();
        user.setUsername(requestBody.get("username"));
        user.setEnabled(true);
        user.setRoles("USER");
        user.setEmail(requestBody.getOrDefault("email", null));
        user.setPassword(passwordEncoder.encode(requestBody.get("password")));
        userManagementRepository.save(user);

        user = userManagementRepository.findByUsername(user.getUsername());

        URI uri = uriComponentsBuilder
            .path("/home/{username}/profile")
            .buildAndExpand(user.getUsername())
            .toUri();

        return ResponseEntity.created(uri).build();
    }

    @PatchMapping("/{username}/update")
    public ResponseEntity<User> updateUserProfile(
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody HashMap<String, String> requestBody,
        HttpServletRequest request) throws ServletException {
        User user = userManagementRepository.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (requestBody.containsKey("password")) {
            user.setPassword(passwordEncoder.encode(requestBody.get("password")));
        }

        if (requestBody.containsKey("email")) {
            user.setEmail(requestBody.get("email"));
        }

        userManagementRepository.save(user);
        user = userManagementRepository.findByUsername(user.getUsername());
        request.logout();
        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{username}/delete")
    private ResponseEntity<Void> deleteCashCard(
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request) throws ServletException {

        String username = userDetails.getUsername();

        if (userManagementRepository.existsByUsername(username)) {
            request.logout();
            userManagementRepository.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }
}
