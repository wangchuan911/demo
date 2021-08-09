package com.hubidaauto.servmarket.common.entity;

import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
import com.sun.istack.NotNull;
import org.springframework.util.Assert;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname AbstractBaseCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/9 21:46
 */
public class AbstractBaseCondition<K, R> {
    Page page;

    List<R> items;

    public AbstractBaseCondition add(@NotNull R r) {
        Assert.notNull(r, "[itemVO] must not null");
        if (items == null)
            items = new LinkedList<>();
        items.add(r);
        return this;
    }

    public AbstractBaseCondition page(int num) {
        if (this.page == null)
            this.page = new Page(num);
        return this;
    }

    AbstractBaseCondition page(int num, int size) {
        if (this.page == null) {
            page = new Page(num, size);
        }
        return this;
    }

    public Page getPage() {
        return page;
    }

    public void setPage(Page page) {
        this.page = page;
    }

    public List<R> getItems() {
        return items;
    }

    public void setItems(List<R> items) {
        this.items = items;
    }
}
