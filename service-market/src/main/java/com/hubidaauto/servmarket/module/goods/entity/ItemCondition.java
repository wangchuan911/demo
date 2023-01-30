package com.hubidaauto.servmarket.module.goods.entity;


import com.hubidaauto.servmarket.common.entity.AbstractBaseCondition;


public class ItemCondition extends AbstractBaseCondition<Long, ItemVO> {
    String keyWord;

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getKeyWord() {
        return keyWord;
    }
}
