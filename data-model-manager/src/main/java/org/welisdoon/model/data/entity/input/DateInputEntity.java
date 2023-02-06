package org.welisdoon.model.data.entity.input;

import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Input(InputTypes.Date)
public class DateInputEntity extends AbstractInputEntity {
    @Override
    public Object toObject(String value) {
        return LocalDate.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    @Override
    public void inputCheck(Object value) {
        LocalDate date = (LocalDate) value;
    }

    @Override
    public void inputCheck(Object value, ColumnEntity entity) {

    }
}
