package org.welisdoon.model.data.consts;

import org.welisdoon.common.data.IData;
import org.welisdoon.model.data.entity.input.*;

public enum InputTypes implements IData.IDataMarker {
    SingleSelect(SingleSelectInputEntity.class),
    MultiSelect(MultiSelectInputEntity.class),
    Text(TextInputEntity.class),
    Number(NumberInputEntity.class),
    Date(DateInputEntity.class),
    Time(TimeInputEntity.class),
    Search(SearchInputEntity.class),
    Undefine(AbstractInputEntity.Empty.class);

    Class<? extends AbstractInputEntity> inputClass;

    InputTypes(Class<? extends AbstractInputEntity> inputClass) {
        this.inputClass = inputClass;
    }
}
