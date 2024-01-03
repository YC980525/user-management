package com.example.usermanagement;

import java.util.Calendar;
import java.util.Date;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;

@Entity
public class PasswordResetToken {

    private static final int EXPIRATION = 60 * 10;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @OneToOne
    private User user;

    private String tokenValue;
    private Date expiryDate;

    public PasswordResetToken() {}

    public PasswordResetToken(String tokenValue, User user) {
        this.tokenValue = tokenValue;
        this.user = user;
        this.expiryDate = calculateExpiryDate(EXPIRATION);
    }

    private Date calculateExpiryDate(int expiryTimeInMinutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.MINUTE, expiryTimeInMinutes);
        return new Date(cal.getTime().getTime());
    }

    public String getTokenValue() {
        return tokenValue;
    }

    public User getUser() {
        return user;
    }

    public Date getExpiryDate() {
        return expiryDate;
    }

}
