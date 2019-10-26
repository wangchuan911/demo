package pers.welisdoon.webserver.service.custom.entity;

import java.util.List;

import org.apache.ibatis.type.Alias;

public class TacheVO {
    private Integer tacheId;
    private String tacheName;
    private Float seq;
    private List<TacheVO> childTaches;
    private Integer role;
    private Integer tampalateId;
    private Integer parantTacheId;

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


    public Integer getRole() {
        return role;
    }

    public TacheVO setRole(Integer role) {
        this.role = role;
        return this;
    }

    public List<TacheVO> getChildTaches() {
        return childTaches;
    }

    public TacheVO setChildTaches(List<TacheVO> childTaches) {
        this.childTaches = childTaches;
        return this;
    }

    public Integer getTampalateId() {
        return tampalateId;
    }

    public TacheVO setTampalateId(Integer tampalateId) {
        this.tampalateId = tampalateId;
        return this;
    }

    public Integer getParantTacheId() {
        return parantTacheId;
    }

    public TacheVO setParantTacheId(Integer parantTacheId) {
        this.parantTacheId = parantTacheId;
        return this;
    }
}
