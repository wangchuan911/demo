package org.welisdoon.web.common;

import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @Classname CallChain
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/1/27 01:53
 */
public class Chain<T> {
    List<T> chain = null;

    public Chain add(T fun) {
        chain.add(fun);
        return this;
    }

    public Chain() {
        chain = new LinkedList<>();
    }

    public void release() {
        chain.clear();
        SoftReference<List<T>> reference = new SoftReference<>(chain);
        chain = null;
        reference.clear();
    }

    public Iterator<T> iterator(){
        return chain.iterator();
    }
}
