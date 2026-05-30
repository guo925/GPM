package com.gjx.gpms.cache;

/**
 * 系统缓存 Key 统一定义。
 */
public final class CacheKeys {

    public static final String DASHBOARD_STATISTICS = "dashboard:statistics";
    public static final String BATCH_CURRENT = "batch:current";
    public static final String TOPIC_HOT_LIST = "topic:hot:list";

    private CacheKeys() {
    }

    public static String topicDetail(Long id) {
        return "topic:detail:" + id;
    }

    public static String userMenu(Long userId) {
        return "user:menu:" + userId;
    }

    public static String permission(Long userId) {
        return "permission:" + userId;
    }

    public static String loginToken(String token) {
        return "login:token:" + token;
    }

    public static String loginTokenByUserId(Long userId) {
        return "login:token:user:" + userId;
    }

    public static String selectionQuota(Long topicId) {
        return "selection:topic:quota:" + topicId;
    }

    public static String selectionStudent(Long batchId, Long studentId) {
        return "selection:student:" + batchId + ":" + studentId;
    }
}
