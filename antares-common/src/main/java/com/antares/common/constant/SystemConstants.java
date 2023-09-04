package com.antares.common.constant;

public interface SystemConstants {
    //发送验证码形式
    public static final int PHONE_CODE = 0;
    public static final int MAIL_CODE = 1;
    //用户名前缀
    public static final String USERNAME_PREFIX = "antares_uid";
    //登录用户
    public static final String LOGIN_USER = "login_user";
    //标签颜色
    public static final String[] TAG_COLORS = new String[]{"pink", "red", "orange", "green", "cyan", "blue", "purple"};

    //登录态cookie的name
    public static final String TOKEN = "TOKEN";

    //推荐用户的相似度阈值
    public static final double RECOMMEND_THRESHOLD = 0.6;
    public static final int RANDOM_RECOMMEND_BATCH_SIZE = 50;
    public static final int RECOMMEND_SIZE = 8;

    //文章的一些属性
    public static final int ARTICLE_STATUS_PUBLISHED = 1;
    public static final int ARTICLE_STATUS_DRAFT = 0;
    public static final int ARTICLE_PRIME = 1;
    public static final int ARTICLE_COMMON = 0;
    public static final int ARTICLE_GLOBAL_TOP = 1;

    //消息的一些属性（类型）
    public static final int NOTIFICATION_LIKE = 1;
    public static final int NOTIFICATION_COMMENT = 2;
    public static final int NOTIFICATION_MSG = 3;
    public static final int NOTIFICATION_NOTICE = 4;

}

