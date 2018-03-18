package com.ironman.forum.form;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BlogPublishForm {
    @NotBlank(message = "标题不能为空")
    private String title;
    @NotBlank(message = "内容不能为空")
    private String content;
    @NotNull(message = "权限不能为空")
    private Boolean isPrivate;
    @NotNull(message = "是否为分享不能为空")
    private Boolean isShare;
    //原博客uniqueId
    private String originId;
}
