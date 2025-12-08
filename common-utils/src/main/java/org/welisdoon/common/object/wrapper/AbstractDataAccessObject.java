package org.welisdoon.common.object.wrapper;

/**
 * @Classname AbstractDataAccessObject
 * @Description TODO
 * @Author Septem
 * @Date 9:32
 */
public abstract class AbstractDataAccessObject implements IDataAccessObject {
    public AbstractDataAccessObject() {
        IDataAccessObject.initialization(this.getClass());
    }
}
