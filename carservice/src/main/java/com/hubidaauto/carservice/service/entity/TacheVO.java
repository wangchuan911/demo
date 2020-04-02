package com.hubidaauto.carservice.service.entity;

import java.util.List;

public class TacheVO {
    private Integer tacheId;
    private String tacheName;
    private Float seq;
    private Integer tampalateId;
    private List<TacheRela> tacheRelas;
    private String code;
    private Integer nextTache;
    private Integer superTache;

    private List<PushConfig> pushConfigs;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Integer getTacheId() {
        return tacheId;
    }

    public TacheVO setTacheId(Integer tacheId) {
        this.tacheId = tacheId;
        return this;
    }

    public String getTacheName() {
        return tacheName;
    }

    public TacheVO setTacheName(String tacheName) {
        this.tacheName = tacheName;
        return this;
    }

    public Float getSeq() {
        return seq;
    }

    public TacheVO setSeq(Float seq) {
        this.seq = seq;
        return this;
    }


    public Integer getTampalateId() {
        return tampalateId;
    }

    public TacheVO setTampalateId(Integer tampalateId) {
        this.tampalateId = tampalateId;
        return this;
    }


    public List<TacheRela> getTacheRelas() {
        return tacheRelas;
    }

    public void setTacheRelas(List<TacheRela> tacheRelas) {
        this.tacheRelas = tacheRelas;
    }

    public static class TacheRela {

        private Integer role;

        private List<TacheVO> childTaches;

        public Integer getRole() {
            return role;
        }

        public TacheRela setRole(Integer role) {
            this.role = role;
            return this;
        }

        public List<TacheVO> getChildTaches() {
            return childTaches;
        }

        public TacheRela setChildTaches(List<TacheVO> childTaches) {
            this.childTaches = childTaches;
            return this;
        }
    }

    public Integer getNextTache() {
        return nextTache;
    }

    public void setNextTache(Integer nextTache) {
        this.nextTache = nextTache;
    }

    public Integer getSuperTache() {
        return superTache;
    }

    public void setSuperTache(Integer superTache) {
        this.superTache = superTache;
    }

    public static class PushConfig {
        String templateId;
        Integer roleId;

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public void setRoleId(Integer roleId) {
            this.roleId = roleId;
        }
    }

    public List<PushConfig> getPushConfigs() {
        return pushConfigs;
    }

    public void setPushConfigs(List<PushConfig> pushConfigs) {
        this.pushConfigs = pushConfigs;
    }
}
