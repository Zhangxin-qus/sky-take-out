package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<String> userThreadLocal = new ThreadLocal<>();

    // ====== 员工 ======
    public static void setCurrentId(Long id) {
        threadLocal.set(id);
    }

    public static Long getCurrentId() {
        return threadLocal.get();
    }

    public static void removeCurrentId() {
        threadLocal.remove();
    }

    // ====== 用户 ======
    public static void setCurrentUserId(String userId) {
        userThreadLocal.set(userId);
    }
    public static String getCurrentUserId() {
        return userThreadLocal.get();
    }
    public static void removeCurrentUserId() {
        userThreadLocal.remove();
    }

}
