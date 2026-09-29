package org.welisdoon.common.object;

import java.util.List;

/**
 * @Classname IParent
 * @Description TODO
 * @Author Septem
 * @Date 10:03
 */
public interface IChildren<T> {
    List<T> getChildren();


    <V extends IChild> V setChildren(T... t);

    <V extends IChild> V addChildren(T... t);

    default void bindParent(T... ts) {
        for (T t : ts) {
            if (t instanceof IParent) {
                ((IParent) t).setParent(this);
            }
        }
    }
}
