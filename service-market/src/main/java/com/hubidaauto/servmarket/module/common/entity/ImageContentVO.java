package com.hubidaauto.servmarket.module.common.entity;

/**
 * @Classname TextContentVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/11 20:08
 */
public class ImageContentVO extends ContentVO<byte[]> {
    Long refId;

    public Long getRefId() {
        return refId;
    }

    public void setRefId(Long refId) {
        this.refId = refId;
    }
}
