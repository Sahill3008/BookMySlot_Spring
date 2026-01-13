package com.secure.appointment.entity;

public enum Role {
    ROLE_CUSTOMER,
    ROLE_PROVIDER,
    ROLE_ADMIN;

    @com.fasterxml.jackson.annotation.JsonCreator
    public static Role fromString(String value) {
        if (value == null) {
            return null;
        }
        String upperValue = value.toUpperCase();
        if (!upperValue.startsWith("ROLE_")) {
            upperValue = "ROLE_" + upperValue;
        }
        try {
            return Role.valueOf(upperValue);
        } catch (IllegalArgumentException e) {
            // Allow failing if it still doesn't match, or return null/default
            throw new IllegalArgumentException("Unknown role: " + value);
        }
    }
}
