package com.hubidaauto.carservice.wxapp.core.entity;

import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

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

    public TacheVO setTacheRelas(List<TacheRela> tacheRelas) {
        this.tacheRelas = tacheRelas;
        return this;
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

    public TacheVO setNextTache(Integer nextTache) {
        this.nextTache = nextTache;
        return this;
    }

    public Integer getSuperTache() {
        return superTache;
    }

    public TacheVO setSuperTache(Integer superTache) {
        this.superTache = superTache;
        return this;
    }

    public static class PushConfig {
        String templateId;
        Integer roleId;
        String values;
        Map<String, String> valueMap;
        String jump;

        public String getTemplateId() {
            return templateId;
        }

        public PushConfig setTemplateId(String templateId) {
            this.templateId = templateId;
            return this;
        }

        public Integer getRoleId() {
            return roleId;
        }

        public PushConfig setRoleId(Integer roleId) {
            this.roleId = roleId;
            return this;
        }

        public String getValues() {
            return openData ? null : values;
        }

        public void setValues(String values) {
            this.values = values;
            valuesToMap();
        }

        public String getJump() {
            return jump;
        }

        public void setJump(String jump) {
            this.jump = jump;
        }

        public Map<String, String> valuesToMap() {
            if (StringUtils.isEmpty(values)) return null;
            if (CollectionUtils.isEmpty(valueMap)) {
                this.valueMap = Map.ofEntries(Arrays.stream(values.split("&&")).map(s -> {
                    int i = s.indexOf("="), j;
                    StringBuilder value = new StringBuilder(s.substring(i + 1));
                    if (value.charAt(0) == '"' && value.charAt(value.length() - 1) == '"') {

                    } /*else if ((j = value.indexOf(".")) > 0) {
                        int k = j + 1, l = j + 2;
                        String word = value.substring(k, l);
                        value = value.replace(k, l, word.toUpperCase());
                    }*/
                    return Map.entry(s.substring(0, i), value.toString());
                }).toArray(Map.Entry[]::new));
            }
            return valueMap;
        }

        private boolean openData = true;

        public PushConfig openData(boolean sw) {
            this.openData = sw;
            return this;
        }
    }

    public List<PushConfig> getPushConfigs() {
        return this.pushConfigs;
    }

    public TacheVO setPushConfigs(List<PushConfig> pushConfigs) {
        this.pushConfigs = pushConfigs;
        return this;
    }

}
