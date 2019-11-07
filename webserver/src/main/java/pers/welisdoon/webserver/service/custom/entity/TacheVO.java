package pers.welisdoon.webserver.service.custom.entity;

import java.util.List;

public class TacheVO {
    private Integer tacheId;
    private String tacheName;
    private Float seq;
    private Integer tampalateId;
    private List<TacheRela> tacheRelas;
    private String code;
    private TacheVO nextTache;

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

    public TacheVO getNextTache() {
        return nextTache;
    }

    public void setNextTache(TacheVO nextTache) {
        this.nextTache = nextTache;
    }
}
