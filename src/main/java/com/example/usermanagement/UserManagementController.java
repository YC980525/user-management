package com.example.usermanagement;

import java.net.URI;
import java.util.HashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired
    private PasswordResetService passwordResetService;

    @GetMapping("/admin/all-users")
    public @ResponseBody Iterable<User> getAllUsers() {
        return userManagementRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<String> logIn(
        @AuthenticationPrincipal UserDetails userDetails,
        UriComponentsBuilder uriComponentsBuilder) {

        var uri = uriComponentsBuilder
            .path("/home/{username}/profile")
            .buildAndExpand(userDetails.getUsername())
            .toUri();

        return ResponseEntity.status(HttpStatus.FOUND).location(uri).build();
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

    @GetMapping("/{username}/profile")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<User> getUserProfile(
        @PathVariable String username,
        @AuthenticationPrincipal UserDetails userDetails) {

        User user = userManagementRepository.findByUsername(username);

        if (user != null) {
            return ResponseEntity.ok(user);
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{username}/update")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<User> updateUserProfile(
        @PathVariable String username,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestBody HashMap<String, String> requestBody,
        HttpServletRequest request) throws ServletException {

        User user = userManagementRepository.findByUsername(userDetails.getUsername());

        if (user == null) {
            return ResponseEntity.notFound().build();
        }

        if (requestBody.containsKey("password")) {
            user.setPassword(passwordEncoder.encode(requestBody.get("password")));
            request.logout();
        }

        if (requestBody.containsKey("email")) {
            user.setEmail(requestBody.get("email"));
        }

        userManagementRepository.save(user);
        user = userManagementRepository.findByUsername(user.getUsername());

        return ResponseEntity.ok(user);
    }

    @DeleteMapping("/{username}/delete")
    @PreAuthorize("#username == authentication.principal.username")
    public ResponseEntity<User> deleteUser(
        @PathVariable String username,
        @AuthenticationPrincipal UserDetails userDetails,
        HttpServletRequest request) throws ServletException {

        log.debug(userManagementRepository.findByUsername(username).toString());

        if (userManagementRepository.existsByUsername(username)) {
            request.logout();
            userManagementRepository.deleteByUsername(username);
            return ResponseEntity.noContent().build();
        }
        else {
            return ResponseEntity.notFound().build();
        }
    }

    @RequestMapping(
        path = {
            "/sign-up", "/login", "/logout",
            "forget-password", "reset-password",
            "/{username}/update", "/{username}/delete",
        },
        method = RequestMethod.GET)
    public ResponseEntity<Void> getCsrfToken(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        csrfToken.getToken();
        return ResponseEntity.ok().build();
    }

    @PostMapping("/forget-password")
    public ResponseEntity<String> forgetPassword(
        @RequestBody HashMap<String, String> requestBody,
        UriComponentsBuilder uriComponentsBuilder) {
        User user = userManagementRepository.findByEmail(requestBody.get("email"));

        if (user == null) {
            log.debug("No user exists with email " + requestBody.get("email"));
            return ResponseEntity.badRequest().build();
        }

        log.debug("Handling forget password request");
        passwordResetService.handleForgetPasswordRequest(user, uriComponentsBuilder);

        return ResponseEntity.ok().body("Reset link sent. Please check your email.");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(
        @RequestParam("token") String tokenValue,
        @RequestBody HashMap<String, String> requestBody) {
        User user = passwordResetService.handleResetPasswordRequest(tokenValue);

        if (user == null) {
            return ResponseEntity.badRequest().build();
        }

        user.setPassword(passwordEncoder.encode(requestBody.get("password")));

        userManagementRepository.save(user);

        return ResponseEntity.ok().body("Password reset.");
    }


}
