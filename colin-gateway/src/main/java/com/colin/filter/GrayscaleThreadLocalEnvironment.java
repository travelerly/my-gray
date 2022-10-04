package com.colin.filter;

/**
 * @author colin
 * @create 2022-10-02 17:48
 */
public class GrayscaleThreadLocalEnvironment {
    private static ThreadLocal<String> threadLocal = new ThreadLocal<String>();

    /**
     * 设置当前线程对应的版本
     *
     * @param currentEnvironmentVsersion
     */
    public static void setCurrentEnvironment(String currentEnvironmentVsersion) {
        threadLocal.set(currentEnvironmentVsersion);
    }

    /**
     * 获取当前环境配置
     *
     * @return
     */
    public static String getCurrentEnvironment() {
        return threadLocal.get();
    }
}
