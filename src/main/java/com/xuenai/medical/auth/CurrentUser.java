package com.xuenai.medical.auth;

import java.io.Serializable;
import java.util.List;

public record CurrentUser(
        Long id,
        String username,
        String realName,
        List<String> roles
) implements Serializable {

    public boolean hasAnyRole(String[] roleCodes) {
        if (roleCodes == null || roleCodes.length == 0) {
            return true;
        }
        for (String roleCode : roleCodes) {
            if (roles.contains(roleCode)) {
                return true;
            }
        }
        return false;
    }
}

