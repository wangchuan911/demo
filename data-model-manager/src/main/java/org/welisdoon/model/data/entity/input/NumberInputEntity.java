package org.welisdoon.model.data.entity.input;

import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

import java.math.BigDecimal;

@Input(InputTypes.Number)
public class NumberInputEntity extends AbstractInputEntity {
    @Override
    public Object toObject(String value) {
        return new BigDecimal(value);
    }

    @Override
    public void inputCheck(Object value) {

    }

    @Override
    public void inputCheck(Object value, ColumnEntity entity) {

    }
}
