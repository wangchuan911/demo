package org.welisdoon.common.object;

/**
 * @Classname IParent
 * @Description TODO
 * @Author Septem
 * @Date 10:03
 */
public interface IParent<T> {

    T getParent();

    <V extends IParent> V setParent(T t);

    default void bindParent(T t) {
        if (t instanceof IChildren) {
            ((IChildren) t).addChildren(this);
        } else if (t instanceof IChild) {
            ((IChild) t).setChild(this);
        }
    }
}
