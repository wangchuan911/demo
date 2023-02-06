package org.welisdoon.model.data.entity.input;

import com.alibaba.fastjson.JSONArray;
import org.springframework.util.Assert;
import org.welisdoon.model.data.annotations.Input;
import org.welisdoon.model.data.consts.InputTypes;

@Input(InputTypes.SingleSelect)
public class SingleSelectInputEntity extends MultiSelectInputEntity {

    @Override
    public void inputCheck(Object value) {
        super.inputCheck(value);
        Assert.isTrue(((JSONArray) value).size() <= 1, "too many value!");
    }
}
