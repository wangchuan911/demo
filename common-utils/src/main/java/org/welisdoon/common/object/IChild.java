package org.welisdoon.common.object;

/**
 * @Classname IParent
 * @Description TODO
 * @Author Septem
 * @Date 10:03
 */
public interface IChild<T> {

    T getChild();

    <V extends IChild> V setChild(T t);

    default void bindParent(T t) {
        if (t instanceof IParent) {
            ((IParent) t).setParent(this);
        }
    }
}
