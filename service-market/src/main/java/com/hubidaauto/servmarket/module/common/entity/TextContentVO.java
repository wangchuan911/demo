package com.hubidaauto.servmarket.module.common.entity;

/**
 * @Classname TextContentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 20:08
 */
public class TextContentVO extends ContentVO<String> {
    Long[] imgIds;

    public Long[] getImgIds() {
        return imgIds;
    }

    public void setImgIds(Long[] imgIds) {
        this.imgIds = imgIds;
    }
}
