package com.xuenai.medical.auth;

public final class UserContext {

    private static final ThreadLocal<CurrentUser> HOLDER = new ThreadLocal<>();

    private UserContext() {
    }

    public static void set(CurrentUser user) {
        HOLDER.set(user);
    }

    public static CurrentUser get() {
        return HOLDER.get();
    }

    public static Long getUserId() {
        CurrentUser user = HOLDER.get();
        return user == null ? null : user.id();
    }

    public static void clear() {
        HOLDER.remove();
    }
}

