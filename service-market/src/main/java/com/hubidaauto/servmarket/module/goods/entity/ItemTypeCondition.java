package com.hubidaauto.servmarket.module.goods.entity;

import com.sun.istack.NotNull;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname ItemTypeCondion
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/5 20:13
 */
public class ItemTypeCondition {
    List<ItemTypeVO> itemTypes;

    ItemTypeCondition add(@NotNull ItemTypeVO itemTypeVO) {
        Assert.notNull(itemTypeVO, "[itemTypeVO] must not null");
        if (itemTypes == null)
            itemTypes = new LinkedList<>();
        itemTypes.add(itemTypeVO);

        return this;
    }
}
