package com.hubidaauto.servmarket.common.entity;

/**
 * @Classname PageCondition
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/11/24 15:43
 */
public abstract class PageCondition {
    Page page;

    public PageCondition page(int num) {
        if (this.page == null)
            this.page = new Page(num);
        return this;
    }

    PageCondition page(int num, int size) {
        if (this.page == null) {
            page = new Page(num, size);
        }
        return this;
    }

    public Page getPage() {
        return page;
    }

    public PageCondition setPage(Page page) {
        this.page = page;
        return this;
    }
}
