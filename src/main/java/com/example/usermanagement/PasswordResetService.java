package com.example.usermanagement;

import java.net.URI;
import java.util.Date;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class PasswordResetService {

    @Autowired
    public PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    public EmailService emailService;

    public void handleForgetPasswordRequest(User user, UriComponentsBuilder uriComponentsBuilder) {

        PasswordResetToken token = generateToken(user);
        log.debug("Saving token with value " + token.getTokenValue());
        passwordResetTokenRepository.save(token);
        URI uri = uriComponentsBuilder
            .path("/home/reset-password")
            .queryParam("token", token.getTokenValue())
            .build()
            .toUri();

        emailService.sendSimpleEmail(
            user.getEmail(),
            "Reset Password",
            "Click the link to reset your password: " + uri.toString());
    }

    public PasswordResetToken generateToken(User user) {
        return new PasswordResetToken(UUID.randomUUID().toString(), user);
    }

    public User handleResetPasswordRequest(String tokenValue) {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenValue(tokenValue);

        if (token == null || isTokenExpired(token)) {
            return null;
        }
        passwordResetTokenRepository.deleteByTokenValue(tokenValue);

        return token.getUser();
    }

    public boolean isTokenExpired(PasswordResetToken token) {
        Date expiration = token.getExpiryDate();
        Date currentDate = new Date();
        return expiration.before(currentDate);
    }
}
