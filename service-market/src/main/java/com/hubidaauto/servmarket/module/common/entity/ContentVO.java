package com.hubidaauto.servmarket.module.common.entity;

/**
 * @Classname ContentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 20:00
 */
public abstract class ContentVO<T> {
    Long id, type;

    T content;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }


    public T getContent() {
        return content;
    }

    public void setContent(T content) {
        this.content = content;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

}
