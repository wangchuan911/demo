package org.welisdoom.task.xml.intf.type;

/**
 * @Classname Script
 * @Description TODO
 * @Author Septem
 * @Date 9:34
 */
public interface Script <T extends Context> {
    default String getScript(T request, String split) {
        return " ";
    }

    default boolean isStaticContent() {
        return false;
    }
}
