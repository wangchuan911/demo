package org.welisdoom.task.xml.intf.type;

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
    default String getScript(Map<String, Object> data, String split) {
        return " ";
    }
}
