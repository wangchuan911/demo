package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 19:33
 */
@Tag(value = "sql", parentTagTypes = {Select.class, Initialization.class}, desc = "sql脚本内容")
public class Sql extends Unit implements Script, Copyable {
    public String getScript(TaskRequest request, String s) {
        return textFormat(request, children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, s).trim()).collect(Collectors.joining(s)));
    }

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }
}
