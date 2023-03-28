package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.ConditionHandler;
import org.welisdoom.task.xml.intf.DataHandler;

import java.util.Map;

/**
 * @Classname SqlIf
 * @Description TODO
 * @Author Septem
 * @Date 17:57
 */
@Tag(value = "if", parentTag = "sql")
public class SqlIf extends Unit implements ConditionHandler {
    @Override
    public void onCondition(Map<String, Object> map) {
        for (Unit child : children) {
            if (child instanceof ConditionHandler) {

            } else if (child instanceof DataHandler) {
                ((DataHandler) child).onData(map);
            }
        }
    }
}
