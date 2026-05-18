package org.example.mall_tiny01.component;


public class UserContext {
    private static final ThreadLocal<String> userHolder = new ThreadLocal<>();
    private static final ThreadLocal<Long> memberIdHolder = new ThreadLocal<>();

    public static void setUsername(String username) {
        userHolder.set(username);
    }

    public static String getUsername() {
        return userHolder.get();
    }

    public static void setCurrentMemberId(Long memberId) {
        memberIdHolder.set(memberId);
    }

    public static Long getCurrentMemberId() {
        return memberIdHolder.get();
    }

    public static void clear() {
        userHolder.remove();
        memberIdHolder.remove();
    }
}
