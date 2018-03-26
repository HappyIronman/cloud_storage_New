package com.ironman.forum.util;

/**
 * @Author: Ironman
 * @Description:
 * @Date: Created in 11:57 2017/12/31 0031
 **/
public class IronConstant {
    public static final String SESSION_USER_KEY = "SESSION_USER_KEY";
    public static final String SESSION_ROLE_KEY = "SESSION_ROLE_KEY";
    public static final String TABLE_USER = "user";
    public static final String TABLE_COMMENT = "comment";
    public static final String TABLE_MOMENT = "moment";
    public static final String TABLE_BLOG = "blog";
    public static final String TABLE_QUESTION = "question";
    public static final String USER_PROPERTY_MOMENT_NUM = "moment_num";
    public static final String USER_PROPERTY_BLOG_NUM = "blog_num";
    public static final String USER_PROPERTY_QUESTION_NUM = "question_num";
    public static final String ARTICLE_PROPERTY_LIKE_NUM = "like_num";
    public static final String ARTICLE_PROPERTY_DISLIKE_NUM = "dislike_num";
    public static final String ARTICLE_PROPERTY_SHARE_NUM = "share_num";
    public static final String ARTICLE_PROPERTY_VIEW_NUM = "view_num";
    public static final int MOMENT_MAX_LENGTH = 80;
    public static final int BLOG_MAX_LENGTH = 100;
    //����δ���޻��߲ȹ�
    public static final int LIKE_CONDITION_DEFAULT = 1;
    //�����ѱ��޹�
    public static final int LIKE_CONDITION_LIKED = 2;
    //�����ѱ��ȹ�
    public static final int LIKE_CONDITION_DISLIKED = 3;
}