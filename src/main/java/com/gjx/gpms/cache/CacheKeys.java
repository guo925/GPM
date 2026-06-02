package com.gjx.gpms.cache;

/**
 * 系统缓存 Key 统一定义。
 */
public final class CacheKeys {

    public static final String DASHBOARD_STATISTICS = "dashboard:statistics";
    public static final String BATCH_CURRENT = "batch:current";
    public static final String TOPIC_HOT_LIST = "topic:hot:list";

    /**
     * 处理CacheKeys相关逻辑。
     */
    private CacheKeys() {
    }

    /**
     * 转换detail相关逻辑。
     */
    public static String topicDetail(Long id) {
        return "topic:detail:" + id;
    }

    /**
     * 处理userMenu相关逻辑。
     */
    public static String userMenu(Long userId) {
        return "user:menu:" + userId;
    }

    /**
     * 处理permission相关逻辑。
     */
    public static String permission(Long userId) {
        return "permission:" + userId;
    }

    /**
     * 登录token相关逻辑。
     */
    public static String loginToken(String token) {
        return "login:token:" + token;
    }

    /**
     * 登录token by user id相关逻辑。
     */
    public static String loginTokenByUserId(Long userId) {
        return "login:token:user:" + userId;
    }

    /**
     * 查询quota相关逻辑。
     */
    public static String selectionQuota(Long topicId) {
        return "selection:topic:quota:" + topicId;
    }

    /**
     * 查询student相关逻辑。
     */
    public static String selectionStudent(Long batchId, Long studentId) {
        return "selection:student:" + batchId + ":" + studentId;
    }
}
