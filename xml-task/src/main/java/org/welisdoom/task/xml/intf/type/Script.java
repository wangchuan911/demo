package org.welisdoom.task.xml.intf.type;

import org.welisdoom.task.xml.entity.TaskInstance;

/**
 * @Classname Script
 * @Description TODO
 * @Author Septem
 * @Date 9:34
 */
public interface Script extends UnitType {
    default String getScript(TaskInstance request, String split) {
        return " ";
    }

    default boolean isStaticContent() {
        return false;
    }
}
