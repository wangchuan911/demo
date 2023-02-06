package org.welisdoon.model.data.entity.input;


import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

public abstract class AbstractInputEntity extends IData.DataObject<Long, InputTypes> {
    public final static AbstractInputEntity EMPTY = new Empty() {
    };

    @Override
    public InputTypes getDataMarker() {
        return super.getDataMarker() == null ? this.getClass().getAnnotation(Input.class).value() : super.getDataMarker();
    }

    public static class Empty extends AbstractInputEntity {


        @Override
        public Object toObject(String value) {
            return null;
        }

        @Override
        public void inputCheck(Object value) {

        }

        @Override
        public void inputCheck(Object value, ColumnEntity entity) {

        }
    }

    public abstract Object toObject(String value);

    public abstract void inputCheck(Object value);

    public abstract void inputCheck(Object value, ColumnEntity entity);
}
