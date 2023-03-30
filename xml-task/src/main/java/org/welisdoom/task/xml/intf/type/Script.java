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
    default String getScript(Map<String, Object> data) {
        return " ";
    }

    static String getScript(Map<String, Object> data, Script... scripts) {
        return Arrays.stream(scripts).map(script -> script.getScript(data)).collect(Collectors.joining(" "));
    }

    static String getScript(Map<String, Object> data, Stream<Script> stream) {
        return stream.map(script -> script.getScript(data)).collect(Collectors.joining(" "));
    }

    static String trim(String content) {
        boolean left = Character.isWhitespace(content.charAt(0));
        boolean right = Character.isWhitespace(content.charAt(content.length() - 1));
        return String.format("%s%s%s", left ? " " : "", content.trim(), right ? " " : "");
    }
}
