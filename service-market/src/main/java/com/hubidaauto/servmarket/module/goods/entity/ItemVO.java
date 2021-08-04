package com.hubidaauto.servmarket.module.goods.entity;

public class ItemVO {
    Long id, type, price;
    String name, content;

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

    public String getContent() {
        return content;
    }

    public ItemVO setContent(String content) {
        this.content = content;
        return this;
    }
}
