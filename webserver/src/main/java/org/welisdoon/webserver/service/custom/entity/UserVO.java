package org.welisdoon.webserver.service.custom.entity;

public class UserVO {
    private String id;
    private String name;
    private Integer role;
    private Integer maxRole;
    private String phone;
    private UserAttr userAttr;
    private String sessionKey;
    private boolean openData = true;

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

    public Integer getRole() {
        return role;
    }

    public UserVO setRole(Integer role) {
        this.role = role;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public UserVO setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getSessionKey() {
        return this.openData ? null : sessionKey;
    }

    public UserVO setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
        return this;
    }

    public UserVO openData(boolean sw) {
        this.openData = sw;
        return this;
    }

    public UserAttr getUserAttr() {
        return userAttr;
    }

    public UserVO setUserAttr(UserAttr userAttr) {
        this.userAttr = userAttr;
        return this;
    }

    public Integer getMaxRole() {
        return maxRole;
    }

    public UserVO setMaxRole(Integer maxRole) {
        this.maxRole = maxRole;
        return this;
    }

    static class UserAttr {
        private boolean vip;
        private Integer[] regionCode;

        public boolean isVip() {
            return vip;
        }

        public UserAttr setVip(boolean vip) {
            this.vip = vip;
            return this;
        }

        public Integer[] getRegionCode() {
            return regionCode;
        }

        public UserAttr setRegionCode(Integer[] regionCode) {
            this.regionCode = regionCode;
            return this;
        }
    }
}
