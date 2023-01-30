package com.hubidaauto.servmarket.module.region.entity;

import java.util.List;

/**
 * @Classname RegionVO
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/9/11 13:33
 */
public class RegionVO {
    Long id;
    String name;
    List<RegionVO> childs;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<RegionVO> getChilds() {
        return childs;
    }

    public void setChilds(List<RegionVO> childs) {
        this.childs = childs;
    }
}
