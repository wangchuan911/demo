package org.welisdoom.task.xml.entity;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "values", parentTag = "init")
public class Values extends Unit {
    List<Value> values = new LinkedList<>();

    @Override
    public void nodeEndOnChild(Unit unit) {
        super.nodeEndOnChild(unit);
        values.add((Value) unit);
    }
}
