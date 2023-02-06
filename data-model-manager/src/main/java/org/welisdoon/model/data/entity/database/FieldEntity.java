package org.welisdoon.model.data.entity.database;

import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.annotations.Model;
import org.welisdoon.model.data.consts.DataModelType;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.input.AbstractInputEntity;

@Model(DataModelType.Field)
public class FieldEntity extends AbstractDataEntity {
    ColumnEntity column;
    AbstractInputEntity input = AbstractInputEntity.EMPTY;
    String defaultValue;
    boolean lock = false;

    public InputTypes getInputType() {
        return input == null ? InputTypes.Undefine : input.getClass().getAnnotation(Input.class).value();
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public boolean isLock() {
        return lock;
    }

    public void setLock(boolean lock) {
        this.lock = lock;
    }

    public ColumnEntity getColumn() {
        return column;
    }

    public void setColumn(ColumnEntity column) {
        this.column = column;
    }

    public AbstractInputEntity getInput() {
        return input;
    }

    public void setInput(AbstractInputEntity input) {
        this.input = input;
    }
}

