package org.welisdoom.task.xml.entity;

import org.apache.commons.collections4.MapUtils;
import org.xml.sax.Attributes;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "value", parentTag = Initialization.class)
public class Value extends Unit {

    @Override
    public Unit attr(Attributes attributes) {
        return super.attr(attributes);
    }

    public String getValue() {
        return MapUtils.getString(attributes, "value", this.content);
    }
}
