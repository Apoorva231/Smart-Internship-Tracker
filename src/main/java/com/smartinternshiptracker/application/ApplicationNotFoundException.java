package com.smartinternshiptracker.application;

public class ApplicationNotFoundException extends RuntimeException {

    public ApplicationNotFoundException() {
        super("Application not found");
    }
}