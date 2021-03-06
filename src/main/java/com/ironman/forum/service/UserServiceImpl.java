package com.ironman.forum.service;

import com.ironman.forum.conf.UserLoginUtil;
import com.ironman.forum.dao.*;
import com.ironman.forum.entity.*;
import com.ironman.forum.form.LikeArticleFormBean;
import com.ironman.forum.form.RegisterForm;
import com.ironman.forum.form.UserEditForm;
import com.ironman.forum.form.UserLoginForm;
import com.ironman.forum.util.*;
import com.ironman.forum.vo.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;


@Service
public class UserServiceImpl implements UserService {

    private static Log log = LogFactory.getLog(UserServiceImpl.class);

    @Autowired
    private CommonService commonService;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AboutMeDAO aboutMeDAO;

    @Autowired
    private LikeLogDAO likeLogDAO;

    @Autowired
    private ViewLogDAO viewLogDAO;

    @Autowired
    private FollowDAO followDAO;

    @Autowired
    private CommentDAO commentDAO;

    @Autowired
    private MomentDAO momentDAO;

    @Autowired
    private BlogDAO blogDAO;

    @Autowired
    private QuestionDAO questionDAO;

    @Autowired
    private AnsyService ansyService;

    @Override
    public UserInfoVO getMyBaseInfo() throws GlobalException {
        Long userId = UserLoginUtil.getLoginUserId();
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
        }
        return this.assembleUserInfoVO(user);
    }


    @Override
    public UserInfoVO editInfo(UserEditForm form, HttpSession session) throws GlobalException {
        long userId = UserLoginUtil.getLoginUserId();
        User user = userDAO.getById(userId);
        if (user == null) {
            throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
        }
        user.setProfile(form.getProfile());
        user.setPhone(form.getPhone());
        user.setEmail(form.getEmail());
        user.setSex(form.getSex());
        user.setSchool(form.getSchool());
        user.setIntro(form.getIntro());
        userDAO.update(user);

        this.saveUserSession(session, user);
        return this.assembleUserInfoVO(user);
    }

    @Override
    public List<FollowerVO> pageMyFollowerList(PageRequest pageRequest) throws GlobalException {
        Long userId = UserLoginUtil.getLoginUserId();

        return this.getFollowerListByUserId(userId, pageRequest);
    }

    @Override
    public List<FollowerVO> pageMyFollowingList(PageRequest pageRequest) throws GlobalException {
        Long userId = UserLoginUtil.getLoginUserId();

        return this.getFollowingListByUserId(userId, pageRequest);
    }

    @Override
    public List<BaseLogVO> pageAboutMeList(PageRequest pageRequest) throws GlobalException {
        //У��
        long userId = UserLoginUtil.getLoginUserId();
        List<AboutMe> aboutMeList = aboutMeDAO.getAllLimitByUserId(userId, pageRequest);

        List<BaseLogVO> baseLogVOList = new ArrayList<>();
        if (aboutMeList == null || aboutMeList.size() == 0) {
            return baseLogVOList;
        }

        for (AboutMe aboutMe : aboutMeList) {
            BaseLogVO baseLogVO;
            int aboutmeType = aboutMe.getType();
            long logId = aboutMe.getLogId();
            if (aboutmeType == AboutMe.LogType.LIKE_LOG.getId()) {
                LikeLog likeLog = likeLogDAO.getById(logId);
                if (likeLog == null) {
                    throw new GlobalException(ResponseStatus.LOG_NOT_EXIST);
                }
                baseLogVO = this.assembleLikeLogVO(likeLog);
            } else if (aboutmeType == AboutMe.LogType.VIEW_LOG.getId()) {
                ViewLog viewLog = viewLogDAO.getById(logId);
                if (viewLog == null) {
                    throw new GlobalException(ResponseStatus.LOG_NOT_EXIST);
                }
                baseLogVO = this.assembleViewLogVO(viewLog);
            } else if (aboutmeType == AboutMe.LogType.COMMENT.getId()) {
                Comment comment = commentDAO.getById(logId);
                baseLogVO = this.assembleCommentLogVo(comment);
            } else if (aboutmeType == AboutMe.LogType.FOLLOW.getId()) {
                Follow follow = followDAO.getById(logId);
                if (follow == null) {
                    throw new GlobalException(ResponseStatus.LOG_NOT_EXIST);
                }
                baseLogVO = this.assembleFollowLogVo(follow);
            } else {
                throw new GlobalException(ResponseStatus.SYSTEM_ERROR);
            }

            baseLogVO.setNew(aboutMe.isNew());
            baseLogVO.setCreateTime(aboutMe.getCreateTime());
            baseLogVOList.add(baseLogVO);
        }
        //将user表中的new_about_me_num置为0
        if (pageRequest.getPage() == 0) {
            userDAO.updateNewAboutMeNumById(userId, 0);
        }
        //异步将isNew标志置为false
        ansyService.ansyUpdateAboutMeStatus(aboutMeList);
        return baseLogVOList;
    }


    private CommentLogVO assembleCommentLogVo(Comment comment) throws GlobalException {
        CommentLogVO commentLogVO = new CommentLogVO();
        commentLogVO.setLogType(AboutMe.LogType.COMMENT.getId());
        User user = userDAO.getById(comment.getUserId());
        commentLogVO.setUserId(user.getUniqueId());
        commentLogVO.setUsername(user.getUsername());
        commentLogVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));
        commentLogVO.setUniqueId(comment.getUniqueId());
        commentLogVO.setContent(comment.getContent());

        int type = comment.getType();
        long replyId = comment.getReplyId();
        //获取文章必要信息
        Article article;
        if (type == ArticleTypeEnum.COMMENT.getId()) {
            article = commentDAO.getById(replyId);
            if (article == null) {
                throw new GlobalException(ResponseStatus.COMMENT_NOT_EXIST);
            }
        } else if (type == ArticleTypeEnum.MOMENT.getId()) {
            article = momentDAO.getById(replyId);
            if (article == null) {
                throw new GlobalException(ResponseStatus.MOMENT_NOT_EXIST);
            }
        } else if (type == ArticleTypeEnum.BLOG.getId()) {
            article = blogDAO.getBaseInfoById(replyId);
            if (article == null) {
                throw new GlobalException(ResponseStatus.BLOG_NOT_EXIST);
            }
        } else if (type == ArticleTypeEnum.QUESTION.getId()) {
            article = questionDAO.getBaseInfoById(replyId);
            if (article == null) {
                throw new GlobalException(ResponseStatus.QUESTION_NOT_EXIST);
            }
        } else {
            log.error("文章类型不存在");
            throw new GlobalException();
        }

        commentLogVO.setReplyId(article.getUniqueId());
        commentLogVO.setType(comment.getType());
        commentLogVO.setReplyTitle(this.getCommentReplyTitle(article));
        return commentLogVO;
    }

    private String getCommentReplyTitle(Article article) throws GlobalException {
        if (article instanceof Comment) {
            Comment comment = (Comment) article;
            return IronUtil.getAbstractContentStr(comment.getContent(), IronConstant.TITLE_MAX_LENGTH);
        }
        if (article instanceof Moment) {
            Moment moment = (Moment) article;
            return IronUtil.getAbstractContentStr(moment.getContent(), IronConstant.TITLE_MAX_LENGTH);
        }

        if (article instanceof Blog) {
            Blog blog = (Blog) article;
            return blog.getTitle();
        }

        if (article instanceof Question) {
            Question question = (Question) article;
            return question.getTitle();
        }
        throw new GlobalException(ResponseStatus.ARTICLE_TYPE_ILLEGAL);
    }

    private FollowLogVO assembleFollowLogVo(Follow follow) throws GlobalException {
        FollowLogVO followLogVO = new FollowLogVO();
        followLogVO.setLogType(AboutMe.LogType.FOLLOW.getId());

        User user = userDAO.getById(follow.getFollowerId());
        followLogVO.setUserId(user.getUniqueId());
        followLogVO.setUsername(user.getUsername());
        followLogVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));

        followLogVO.setCreateTime(follow.getCreateTime());

        return followLogVO;
    }

    private ViewLogVO assembleViewLogVO(ViewLog viewLog) throws GlobalException {
        ViewLogVO viewLogVO = new ViewLogVO();
        viewLogVO.setLogType(AboutMe.LogType.VIEW_LOG.getId());

        int articleType = viewLog.getType();
        long targetId = viewLog.getTargetId();

        viewLogVO.setArticleType(articleType);


        if (articleType == ArticleTypeEnum.COMMENT.getId()) {
            //todo
        } else if (articleType == ArticleTypeEnum.MOMENT.getId()) {
            Moment moment = momentDAO.getById(targetId);
            if (moment == null) {
                throw new GlobalException(ResponseStatus.MOMENT_NOT_EXIST);
            }
            viewLogVO.setArticleId(moment.getUniqueId());
            viewLogVO.setArticleContent(moment.getContent());
        } else if (articleType == ArticleTypeEnum.BLOG.getId()) {
            Blog blog = blogDAO.getBaseInfoById(targetId);
            if (blog == null) {
                throw new GlobalException(ResponseStatus.BLOG_NOT_EXIST);
            }
            viewLogVO.setArticleId(blog.getUniqueId());
            viewLogVO.setArticleTitle(blog.getTitle());
        } else if (articleType == ArticleTypeEnum.QUESTION.getId()) {
            //todo
        } else {
            log.error("type���Ͳ��Ϸ�");
            throw new GlobalException();
        }


        //��������
//        if (viewLog.getUserId() == IronConstant.ANONYMOUS_USER_ID) {
//            viewLogVO.setUserId(IronConstant.ANONYMOUS_USER_UNIQUE_ID);
//            viewLogVO.setUsername(IronConstant.ANONYMOUS_USER_NAME);
//            viewLogVO.setProfileUrl(IronConstant.ANONYMOUS_USER_PROFILE_URL);
//        } else {
        User user = userDAO.getById(viewLog.getUserId());
        viewLogVO.setUserId(user.getUniqueId());
        viewLogVO.setUsername(user.getUsername());
        viewLogVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));
//        }

        viewLogVO.setCreateTime(viewLog.getCreateTime());

        return viewLogVO;
    }

    private LikeLogVO assembleLikeLogVO(LikeLog likeLog) throws GlobalException {

        LikeLogVO likeLogVO = new LikeLogVO();
        likeLogVO.setLogType(AboutMe.LogType.LIKE_LOG.getId());
        likeLogVO.setLike(likeLog.isLike());

        int articleType = likeLog.getType();
        long targetId = likeLog.getTargetId();

        likeLogVO.setArticleType(articleType);

        if (articleType == ArticleTypeEnum.COMMENT.getId()) {
            Comment comment = commentDAO.getById(targetId);
            if (comment == null) {
                throw new GlobalException(ResponseStatus.COMMENT_NOT_EXIST);
            }
            likeLogVO.setArticleId(comment.getUniqueId());
            likeLogVO.setArticleContent(comment.getContent());
        } else if (articleType == ArticleTypeEnum.MOMENT.getId()) {
            Moment moment = momentDAO.getById(targetId);
            if (moment == null) {
                throw new GlobalException(ResponseStatus.MOMENT_NOT_EXIST);
            }
            likeLogVO.setArticleId(moment.getUniqueId());
            likeLogVO.setArticleContent(moment.getContent());
        } else if (articleType == ArticleTypeEnum.BLOG.getId()) {
            Blog blog = blogDAO.getBaseInfoById(targetId);
            if (blog == null) {
                throw new GlobalException(ResponseStatus.BLOG_NOT_EXIST);
            }
            likeLogVO.setArticleId(blog.getUniqueId());
            likeLogVO.setArticleTitle(blog.getTitle());
        } else if (articleType == ArticleTypeEnum.QUESTION.getId()) {
            //todo
        } else {
            log.error("type���Ͳ��Ϸ�");
            throw new GlobalException();
        }


        User user = userDAO.getById(likeLog.getUserId());
        likeLogVO.setUserId(user.getUniqueId());
        likeLogVO.setUsername(user.getUsername());
        likeLogVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));

        likeLogVO.setCreateTime(likeLog.getCreateTime());

        return likeLogVO;
    }

    @Override
    public List<FollowerVO> getFollowerListByUserId(Long userId, PageRequest pageRequest) throws GlobalException {
        List<Follow> followList = followDAO.getAllLimitByUserId(userId, pageRequest);
        List<FollowerVO> followerList = new ArrayList<>();
        if (followList == null || followList.size() == 0) {
            return followerList;
        }
        for (Follow follow : followList) {
            User user = userDAO.getById(follow.getFollowerId());
            if (user == null) {
                throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
            }
            FollowerVO followerVO = BeanUtils.copy(user, FollowerVO.class);
            followerVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));
            followerVO.setFollowDate(follow.getCreateTime());

            //���Ƿ������ķ�˿
            if (followDAO.getByFollowerIdAndUserId(userId, follow.getFollowerId()) == null) {
                followerVO.setRelation(UserInfoVO.Relation.HIS_IS_MY_FANS.getId());
            } else {
                followerVO.setRelation(UserInfoVO.Relation.FANS_TO_EACH_OTHER.getId());
            }

            followerList.add(followerVO);
        }
        return followerList;
    }


    private List<FollowerVO> getFollowingListByUserId(Long followerId, PageRequest pageRequest) throws GlobalException {
        List<Follow> followList = followDAO.getAllLimitByFollowerId(followerId, pageRequest);
        List<FollowerVO> followerList = new ArrayList<>();
        if (followList == null || followList.size() == 0) {
            return followerList;
        }
        for (Follow follow : followList) {
            User user = userDAO.getById(follow.getUserId());
            if (user == null) {
                throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
            }
            FollowerVO followerVO = BeanUtils.copy(user, FollowerVO.class);
            followerVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));
            followerVO.setFollowDate(follow.getCreateTime());

            //���Ƿ����ҵķ�˿
            if (followDAO.getByFollowerIdAndUserId(follow.getUserId(), followerId) == null) {
                followerVO.setRelation(UserInfoVO.Relation.I_AM_HIS_FANS.getId());
            } else {
                followerVO.setRelation(UserInfoVO.Relation.FANS_TO_EACH_OTHER.getId());
            }

            followerList.add(followerVO);
        }
        return followerList;
    }

    @Override
    public UserInfoVO getUserBaseInfo(String uniqueId) throws GlobalException {
        User user = userDAO.getByUniqueId(uniqueId);
        if (user == null) {
            throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
        }
        UserInfoVO userInfoVO = this.assembleUserInfoVO(user);

        Long selfId = UserLoginUtil.getLoginUserId();
        userInfoVO.setRelation(this.judgeUserRelation(selfId, user.getId()));
        return userInfoVO;
    }

    @Override
    public int judgeUserRelation(long selfId, long userId) {
        //�û�δ��¼��ֱ�ӷ���İ����
        if (selfId == IronConstant.ANONYMOUS_USER_ID) {
            return UserInfoVO.Relation.STRANGER.getId();
        }
        //���selfId��userId��ȣ�ֱ�ӷ��ػ��۹�ϵ
        if (selfId == userId) {
            return UserInfoVO.Relation.FANS_TO_EACH_OTHER.getId();
        }

        //���Ƿ������ķ�˿
        Follow selfFollow = followDAO.getByFollowerIdAndUserId(selfId, userId);
        //���Ƿ����ҵķ�˿
        Follow userFollow = followDAO.getByFollowerIdAndUserId(userId, selfId);
        if (selfFollow == null) {
            if (userFollow == null) {
                return UserInfoVO.Relation.STRANGER.getId();
            } else {
                return UserInfoVO.Relation.HIS_IS_MY_FANS.getId();
            }
        } else {
            if (userFollow == null) {
                return UserInfoVO.Relation.I_AM_HIS_FANS.getId();
            } else {
                return UserInfoVO.Relation.FANS_TO_EACH_OTHER.getId();
            }
        }
    }

    @Override
    public UserInfoVO userLogin(UserLoginForm form, HttpSession session) throws GlobalException {
        User user = userDAO.getByUsernameAndPassword(form.getUsername(), form.getPassword());
        if (user == null) {
            throw new GlobalException(ResponseStatus.USERNAME_OR_PASSWORD_INCORRECT);
        }
        this.saveUserSession(session, user);
        return this.assembleUserInfoVO(user);
    }

    @Override
    @Transactional
    public int followUser(String userUniqueId) throws GlobalException {
        User user = userDAO.getByUniqueId(userUniqueId);
        if (user == null) {
            throw new GlobalException(ResponseStatus.USER_NOT_EXIST);
        }
        long followerId = UserLoginUtil.getLoginUserId();
        long targetId = user.getId();

        Follow existFollow = followDAO.getByFollowerIdAndUserId(followerId, targetId);
        //�����ظ���ע
        if (existFollow != null) {
            throw new GlobalException(ResponseStatus.DUPLICATE_FOLLOW_LOG);
        }
        //����follow��
        Follow follow = new Follow();
        follow.setFollowerId(followerId);
        follow.setUserId(targetId);
        follow.setDisabled(false);
        Date createTime = new Date();
        follow.setCreateTime(createTime);

        followDAO.save(follow);

        //�첽���ӱ����˷�˿��
        ansyService.ansyChangeEntityPropertyNumById(IronConstant.TABLE_USER, targetId,
                IronConstant.USER_PROPERTY_FOLLOWER_NUM, true);
        //�첽���ӷ�˿��ע��
        ansyService.ansyChangeEntityPropertyNumById(IronConstant.TABLE_USER, followerId,
                IronConstant.USER_PROPERTY_FOLLOWING_NUM, true);


        //�첽����aboutMe
        FollowLog followLog = new FollowLog();
        followLog.setId(follow.getId());
        followLog.setUserId(followerId);
        followLog.setTargetId(targetId);
        followLog.setDisabled(false);
        followLog.setType(ArticleTypeEnum.USER.getId());
        followLog.setCreateTime(createTime);
        ansyService.ansySaveAboutMe(followLog);


        //���Ƿ����ҵķ�˿
        if (followDAO.getByFollowerIdAndUserId(targetId, followerId) == null) {
            //������
            return UserInfoVO.Relation.I_AM_HIS_FANS.getId();
        } else {
            //���ǣ����ϵΪ����
            return UserInfoVO.Relation.FANS_TO_EACH_OTHER.getId();
        }
    }


    @Override
    public void checkPhone(String phone) throws GlobalException {
        User user = userDAO.getByPhone(phone);
        if (user != null) {
            throw new GlobalException(ResponseStatus.DUPLICATE_PHONE);
        }
    }

    @Override
    public void checkUsername(String username) throws GlobalException {
        User user = userDAO.getByUsername(username);
        if (user != null) {
            throw new GlobalException(ResponseStatus.DUPLICATE_USERNAME);
        }
    }

    @Override
    public UserInfoVO register(RegisterForm form, HttpSession session) throws GlobalException {
        //校验逻辑
        User user = new User();
        user.setUniqueId(form.getUsername());
        user.setPhone(form.getPhone());
        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setUniqueId(IronUtil.generateUniqueId());
        user.setDisabled(false);
        user.setCreateTime(new Date());
        user.setProfile(IronConstant.USER_DEFAULT_PROFILE);
        userDAO.save(user);
        this.saveUserSession(session, user);
        return this.assembleUserInfoVO(user);
    }

    @Override
    public void logout(HttpSession session) {
        session.removeAttribute(IronConstant.SESSION_USER_KEY);
        session.removeAttribute(IronConstant.SESSION_ROLE_KEY);
    }

    @Override
    public void likeArticle(LikeArticleFormBean formBean) throws GlobalException {
        Long userId = UserLoginUtil.getLoginUserId();
        int type = formBean.getType();
        Long targetId = commonService.getArticleBaseInfoByUniqueIdAndType(formBean.getTargetId(), type).getId();
        boolean isLike = formBean.isLike();

        LikeLog existLikeLog = likeLogDAO.getByUserIdAndTargetIdAndType(userId, targetId, type);
        if (existLikeLog != null) {
            throw new GlobalException(ResponseStatus.DUPLICATE_LIKE_LOG);
        }


        LikeLog likeLog = new LikeLog();
        likeLog.setUserId(userId);
        likeLog.setTargetId(targetId);
        likeLog.setType(type);
        likeLog.setLike(isLike);
        likeLog.setDisabled(false);
        likeLog.setCreateTime(new Date());

        likeLogDAO.save(likeLog);

        //增加赞数量
        String property = isLike ? IronConstant.ARTICLE_PROPERTY_LIKE_NUM : IronConstant.ARTICLE_PROPERTY_DISLIKE_NUM;
        ansyService.ansyChangeArticlePropertyNum(type, targetId, property, true);

        //异步写入aboutme
        ansyService.ansySaveAboutMe(likeLog);
    }

    @Override
    @Transactional
    public void cancelLikeArticle(LikeArticleFormBean formBean) throws GlobalException {
        Long userId = UserLoginUtil.getLoginUserId();
        int type = formBean.getType();
        Long targetId = commonService.getArticleBaseInfoByUniqueIdAndType(formBean.getTargetId(), type).getId();
        boolean isLike = formBean.isLike();
        LikeLog likeLog = likeLogDAO.getByUserIdAndTargetIdAndType(userId, targetId, type);
        if (null == likeLog) {
            //赞或踩记录不存在
            throw new GlobalException(ResponseStatus.LOG_NOT_EXIST);
        }
        likeLogDAO.updateDisabledByUserIdAndTargetIdAndTypeAndIsLike(userId, targetId, type, isLike);
        //异步减少赞或踩数量
        String property = isLike ? IronConstant.ARTICLE_PROPERTY_LIKE_NUM : IronConstant.ARTICLE_PROPERTY_DISLIKE_NUM;
        ansyService.ansyChangeArticlePropertyNum(type, targetId, property, false);
        //异步删除aboutme
        ansyService.ansyDeleteAboutMe(likeLog);
    }

    @Override
    public int getNewAboutMeNum() {
        long userId = UserLoginUtil.getLoginUserId();
        return userDAO.getNewAboutMeNumById(userId);
    }

    private void saveUserSession(HttpSession session, User user) {
        session.setAttribute(IronConstant.SESSION_USER_KEY, user);
        session.setAttribute(IronConstant.SESSION_ROLE_KEY, Arrays.asList("ROLE_USER"));
    }

    private UserInfoVO assembleUserInfoVO(User user) throws GlobalException {
        UserInfoVO userInfoVO = BeanUtils.copy(user, UserInfoVO.class);
        userInfoVO.setProfileUrl(commonService.concatImageUrl(user.getProfile()));
        return userInfoVO;
    }
}
