package com.hubidaauto.servmarket.module.goods.entity;


import com.sun.istack.NotNull;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

public class ItemCondition {
    List<ItemVO> items;

    ItemCondition add(@NotNull ItemVO itemVO) {
        Assert.notNull(itemVO, "[itemVO] must not null");
        if (items == null)
            items = new LinkedList<>();
        items.add(itemVO);

        return this;
    }
}
