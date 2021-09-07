package com.hubidaauto.servmarket.common.entity;

/**
 * @Classname Page
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/9 21:45
 */
public class Page {
    int size, page, start;

    public Page(int page) {
        this(page, 10);
    }

    public Page(int page, int size) {
        this.size = size;
        this.page = page;
        this.start = (page - 1) * size;
    }

    public int getSize() {
        return size;
    }

    public int getPage() {
        return page;
    }

    public int getStart() {
        return start;
    }
}
