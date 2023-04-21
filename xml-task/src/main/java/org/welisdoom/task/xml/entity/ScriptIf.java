package org.welisdoom.task.xml.entity;


import ognl.OgnlException;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Classname SqlIf
 * @Description TODO
 * @Author Septem
 * @Date 17:57
 */
@Tag(value = "if", parentTagTypes = {Script.class, Instance.class}, desc = "条件判断,返回脚本内容")
@Attr(name = "test", require = true, desc = "表达式")
public class ScriptIf extends Unit implements Script, Copyable {
    @Override
    public String getScript(TaskRequest request, String s) {
        try {
            if (If.test(attributes.get("test"), request.getOgnlContext(), request.getBus())) {
                return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, s).trim()).collect(Collectors.joining(s));
            }
        } catch (OgnlException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return " ";
    }


    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }
}
