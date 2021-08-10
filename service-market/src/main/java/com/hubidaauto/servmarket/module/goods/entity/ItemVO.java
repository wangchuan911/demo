package com.hubidaauto.servmarket.module.goods.entity;

import java.util.List;

public class ItemVO {
    Long id, type, price;
    String name;
    ItemDetailVO detail;
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

    public ItemDetailVO getDetail() {
        return detail;
    }

    public ItemVO setDetail(ItemDetailVO detail) {
        this.detail = detail;
        return this;
    }

    public List<ItemTypeVO> getTypes() {
        return types;
    }

    public void setTypes(List<ItemTypeVO> types) {
        this.types = types;
    }

}
