package com.hubidaauto.servmarket.module.goods.entity;

import com.hubidaauto.servmarket.module.common.entity.ContentVO;
import org.springframework.util.StringUtils;

import java.util.List;

public class ItemVO {
    Long id, type, price, contentId;
    String name, imgs, desc, typesGroupDesc;
    ContentVO detail;
    List<ItemTypeVO> types;


    public Long getId() {
        return id;
    }

    public ItemVO setId(Long id) {
        this.id = id;
        return this;
    }

    public Long getType() {
        return type;
    }

    public ItemVO setType(Long type) {
        this.type = type;
        return this;
    }

    public Long getPrice() {
        return price;
    }

    public ItemVO setPrice(Long price) {
        this.price = price;
        return this;
    }

    public String getName() {
        return name;
    }

    public ItemVO setName(String name) {
        this.name = name;
        return this;
    }

    public ContentVO getDetail() {
        return detail;
    }

    public ItemVO setDetail(ContentVO detail) {
        this.detail = detail;
        return this;
    }

    public List<ItemTypeVO> getTypes() {
        return types;
    }

    public ItemVO setTypes(List<ItemTypeVO> types) {
        this.types = types;
        return this;
    }

    public Long getContentId() {
        return contentId;
    }

    public ItemVO setContentId(Long contentId) {
        this.contentId = contentId;
        return this;
    }

    public String getImgs() {
        return imgs;
    }

    public ItemVO setImgs(String imgs) {
        this.imgs = imgs;
        return this;
    }

    public ItemVO addImg(Long imgId) {
        if (StringUtils.isEmpty(this.imgs)) {
            this.imgs = imgId.toString();
        } else {
            this.imgs += (String.format(",%d", imgId));
        }
        return this;
    }

    public String getDesc() {
        return desc;
    }

    public ItemVO setDesc(String desc) {
        this.desc = desc;
        return this;
    }

    public String getTypesGroupDesc() {
        return typesGroupDesc;
    }

    public void setTypesGroupDesc(String typesGroupDesc) {
        this.typesGroupDesc = typesGroupDesc;
    }
}
