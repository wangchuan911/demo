package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Stream;

/**
 * @Classname Col
 * @Description TODO
 * @Author Septem
 * @Date 12:17
 */

@Tag(value = "col", parentTagTypes = Stream.class)
public class Col extends Unit {
    String name, value, code;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
