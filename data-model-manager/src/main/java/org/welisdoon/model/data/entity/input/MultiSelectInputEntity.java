package org.welisdoon.model.data.entity.input;

import com.alibaba.fastjson.JSONArray;
import org.springframework.util.Assert;
import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;
import org.welisdoon.model.data.entity.database.ColumnEntity;

@Input(InputTypes.MultiSelect)
public class MultiSelectInputEntity extends AbstractInputEntity {


    @Override
    public Object toObject(String value) {
        return JSONArray.parseArray(value);
    }

    @Override
    public void inputCheck(Object value) {
        Assert.isTrue(((JSONArray) value).size() >= 0, "too many value!");
    }

    @Override
    public void inputCheck(Object value, ColumnEntity entity) {

    }
}
