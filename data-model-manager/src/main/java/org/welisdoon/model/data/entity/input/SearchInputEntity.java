package org.welisdoon.model.data.entity.input;

import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

@Input(InputTypes.Search)
public class SearchInputEntity extends AbstractInputEntity{

    @Override
    public Object toObject(String value) {
        return value;
    }

    @Override
    public void inputCheck(Object value) {

    }

    @Override
    public void inputCheck(Object value, ColumnEntity entity) {

    }
}
