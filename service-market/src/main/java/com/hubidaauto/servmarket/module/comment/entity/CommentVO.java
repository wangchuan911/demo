package com.hubidaauto.servmarket.module.comment.entity;

import com.hubidaauto.servmarket.module.common.annotation.ContentType;

/**
 * @Classname EvaluateVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/17 21:38
 */
@ContentType(id=20005,table = "ORDER_COMMENT")
public class CommentVO {
    Long id;
    String comment;
    Short level;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(Short level) {
        this.level = level;
    }
}
