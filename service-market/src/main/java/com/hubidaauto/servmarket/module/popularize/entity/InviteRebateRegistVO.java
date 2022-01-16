package com.hubidaauto.servmarket.module.popularize.entity;

/**
 * @Classname InviteRebateRegistVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2022/1/15 22:09
 */
public class InviteRebateRegistVO {
    Long id;
    String studyLevel, profession, address, channel;
    int workYear;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStudyLevel() {
        return studyLevel;
    }

    public void setStudyLevel(String studyLevel) {
        this.studyLevel = studyLevel;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public int getWorkYear() {
        return workYear;
    }

    public void setWorkYear(int workYear) {
        this.workYear = workYear;
    }
}
