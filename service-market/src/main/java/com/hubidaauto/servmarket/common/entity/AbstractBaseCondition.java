package com.hubidaauto.servmarket.common.entity;

import com.hubidaauto.servmarket.module.goods.entity.ItemCondition;
import com.hubidaauto.servmarket.module.goods.entity.ItemVO;
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

    List<R> datas;

    K id;

    R data;


    public AbstractBaseCondition add(R r) {
        Assert.notNull(r, "[itemVO] must not null");
        if (datas == null)
            datas = new LinkedList<>();
        datas.add(r);
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

    public List<R> getDatas() {
        return datas;
    }

    public void setDatas(List<R> datas) {
        this.datas = datas;
    }

    public K getId() {
        return id;
    }

    public void setId(K id) {
        this.id = id;
    }

    public R getData() {
        return data;
    }

    public void setData(R data) {
        this.data = data;
    }
}
