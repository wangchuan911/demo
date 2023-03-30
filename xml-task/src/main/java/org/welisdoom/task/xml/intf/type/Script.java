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
    default String getScript(Object data) {
        return " ";
    }

    static String getScript(Object data, Script... scripts) {
        return Arrays.stream(scripts).map(script -> script.getScript(data)).collect(Collectors.joining(" "));
    }

    static String getScript(Object data, Stream<Script> stream) {
        return stream.map(script -> script.getScript(data)).collect(Collectors.joining(" "));
    }
}
