package io.roa.secretmanger.Util;


import io.roa.secretmanger.Model.Entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class SecurityContextUtil {

    public User getCurrentUser() {
        return (User) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public UUID getCurrentUserId() {
        return getCurrentUser().getId();
    }
}