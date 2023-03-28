package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.intf.ConditionHandler;
import org.welisdoom.task.xml.intf.DataHandler;

import java.util.Map;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "foreach", parentTag = {"select", "csv"})
public class ForEach extends Unit implements DataHandler {


    @Override
    public void onData(Map<String, Object> data) {
        for (Unit child : children) {
            handler(child, data);
        }
    }
}
