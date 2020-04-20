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

    public UserVO openData(boolean sw) {
        this.openData = sw;
        return this;
    }

    public String getUnionid() {
        return this.openData ? null : unionid;
    }

    public UserVO setUnionid(String unionid) {
        this.unionid = unionid;
        return this;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UserVO{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", unionid='").append(unionid).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
