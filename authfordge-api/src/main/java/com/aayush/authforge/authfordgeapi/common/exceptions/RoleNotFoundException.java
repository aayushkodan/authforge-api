package com.aayush.authforge.authfordgeapi.common.exceptions;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String defaultRoleNotFound) {
        super(defaultRoleNotFound);
    }
}
