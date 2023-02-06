package org.welisdoon.model.data.entity.input;

import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Input(InputTypes.Time)
public class TimeInputEntity extends AbstractInputEntity {
    @Override
    public Object toObject(String value) {
        return LocalDateTime.parse(value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH24:mm:ss"));
    }

    @Override
    public void inputCheck(Object value) {
        LocalDateTime date = (LocalDateTime) value;
    }

    @Override
    public void inputCheck(Object value, ColumnEntity entity) {

    }
}
