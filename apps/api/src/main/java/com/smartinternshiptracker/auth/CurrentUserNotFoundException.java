package com.smartinternshiptracker.auth;

public class CurrentUserNotFoundException extends RuntimeException {

    public CurrentUserNotFoundException() {
        super("Unauthorized");
    }
}
