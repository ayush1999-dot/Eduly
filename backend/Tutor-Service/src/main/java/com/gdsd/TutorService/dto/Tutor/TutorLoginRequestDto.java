package com.gdsd.TutorService.dto.Tutor;

public class TutorLoginRequestDto {
    private String email;
    private String password;

    public TutorLoginRequestDto() {
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}