package pers.welisdoon.webserver.service.custom.entity;

import java.sql.Timestamp;

public class UserVO {
    private String id;
    private String name;
    private Integer role;
    private String phone;
    private Integer level;

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

    public Integer getLevel() {
        return level;
    }

    public UserVO setLevel(Integer level) {
        this.level = level;
        return this;
    }

    public static class RoleConfig {
        private Integer roleId;
        private Integer level;

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(Integer roleId) {
            this.roleId = roleId;
        }

        public Integer getLevel() {
            return level;
        }

        public void setLevel(Integer level) {
            this.level = level;
        }
    }
}
