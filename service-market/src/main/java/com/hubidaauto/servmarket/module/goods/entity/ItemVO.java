package com.hubidaauto.servmarket.module.goods.entity;

import com.hubidaauto.servmarket.module.common.entity.ContentVO;

import java.util.List;

public class ItemVO {
    Long id, type, price, contentId;
    String name;
    ContentVO detail;
    List<ItemTypeVO> types;

    public Long getId() {
        return id;
    }

    public ItemVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getType() {
        return type;
    }

    public ItemVO setType(Long type) {
        this.type = type;
        return this;
    }

    public Long getPrice() {
        return price;
    }

    public ItemVO setPrice(Long price) {
        this.price = price;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemVO setName(String name) {
        this.name = name;
        return this;
    }

    public ContentVO getDetail() {
        return detail;
    }

    public ItemVO setDetail(ContentVO detail) {
        this.detail = detail;
        return this;
    }

    public List<ItemTypeVO> getTypes() {
        return types;
    }

    public ItemVO setTypes(List<ItemTypeVO> types) {
        this.types = types;
        return this;
    }

    public Long getContentId() {
        return contentId;
    }

    public ItemVO setContentId(Long contentId) {
        this.contentId = contentId;
        return this;
    }

}
