package com.sky.context;

public class BaseContext {

    public static ThreadLocal<Long> threadLocal = new ThreadLocal<>();
    private static ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();

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
    public static void setCurrentUserId(Long userId) {
        userThreadLocal.set(userId);
    }
    public static Long getCurrentUserId() {
        return userThreadLocal.get();
    }
    public static void removeCurrentUserId() {
        userThreadLocal.remove();
    }

}
