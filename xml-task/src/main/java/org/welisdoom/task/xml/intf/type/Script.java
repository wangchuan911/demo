package org.welisdoom.task.xml.intf.type;

import org.welisdoom.task.xml.entity.TaskRequest;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname Script
 * @Description TODO
 * @Author Septem
 * @Date 9:34
 */
public interface Script extends UnitType {
    default String getScript(TaskRequest request, String split) {
        return " ";
    }

    default boolean isStaticContent() {
        return false;
    }
}
