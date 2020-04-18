package com.hubidaauto.carservice.officalaccount.entity;

public class UserVO {
    private String id;
    private String name;
    private boolean openData = true;
    private String unionid;

    public String getId() {
        return id;
    }

    public UserVO setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public UserVO setName(String name) {
        this.name = name;
        return this;
    }



    public String getUnionid() {
        return this.openData ? null : unionid;
    }

    public UserVO setUnionid(String unionid) {
        this.unionid = unionid;
        return this;
    }
}
