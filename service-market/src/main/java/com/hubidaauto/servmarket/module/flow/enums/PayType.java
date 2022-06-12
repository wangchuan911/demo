package com.hubidaauto.servmarket.module.flow.enums;

public enum PayType {
    PRE_PAY(15100, "预付款"),
    ASSIGN(15101, "自定义环节付款");
    long id;
    String name;

    PayType(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
