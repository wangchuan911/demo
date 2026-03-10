package org.welisdoon.common.object.wrapper;

import java.util.Map;

/**
 * @Classname AbstractDataAccessObject
 * @Description TODO
 * @Author Septem
 * @Date 9:32
 */
public abstract class AbstractDataAccessObject implements IDataAccessObject {
    Map<String, Prepare.Result> values;
    State state = State.EMPTY;

    public AbstractDataAccessObject(Map<String, Prepare.Result> values) {
        this.initialization(values);
    }

    public void initialization(Map<String, Prepare.Result> values) {
        if (this.values != null) {
            this.values.clear();
            this.values = null;
        }
        this.values = values;
        state = values == null ? State.EMPTY : State.LOADED;
    }

    public AbstractDataAccessObject() {
        IDataAccessObject.initialization(this.getClass());
    }

    public enum State {
        LOADED, EMPTY
    }
}
