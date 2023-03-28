package org.welisdoom.task.xml.entity;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname Instance
 * @Description TODO
 * @Author Septem
 * @Date 19:13
 */
@Tag(value = "instances", parentTag = "int")
public class Instances extends Unit {
    List<Unit> units = new LinkedList<>();

    @Override
    public void nodeEndOnChild(Unit unit) {
        super.nodeEndOnChild(unit);
        units.add(unit);
    }

}
