package org.welisdoon.flow.module.template.entity;

import org.welisdoon.flow.module.flow.entity.Stream;

import java.util.function.Predicate;

/**
 * @Classname LinkFunction
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/20 17:06
 */
public class LinkFunction {
    Long id;
    Predicate<Stream> predicate;

    public void setId(Long id) {
        this.id = id;
    }

    public Predicate<Stream> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<Stream> predicate) {
        this.predicate = predicate;
    }
}
