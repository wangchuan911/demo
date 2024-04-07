package org.welisdoom.task.xml.entity;


import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Classname SqlIf
 * @Description TODO
 * @Author Septem
 * @Date 17:57
 */
@Tag(value = "if", parentTagTypes = {Script.class}, desc = "条件判断,返回脚本内容")
@Attr(name = "test", require = true, desc = "表达式")
public class ScriptIf extends Unit implements Script, Copyable {
    @Override
    public String getScript(TaskInstance request, String s) {
        if (If.test(attributes.get("test"), request.getOgnlContext(), request.getBus())) {
            return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, s).trim()).collect(Collectors.joining(s));
        }
        return " ";
    }

    @Override
    public Copyable copy() {
        return copyableUnit(this);
    }

    @Tag(value = "choice", parentTagTypes = Script.class, desc = "if else")
    public static class Choice extends Unit implements Script {

        @Override
        public String getScript(TaskInstance request, String split) {
            List<String> list = new LinkedList<>();
            boolean isMatched = false;
            for (Unit child : children) {
                if (child instanceof When && isMatched == false) {
                    list.add(((Script) child).getScript(request, split));
                    isMatched = !StringUtils.isAllBlank(list.get(list.size() - 1));
                } else if (child instanceof Otherwise) {
                    if (isMatched) continue;
                    list.add(((Script) child).getScript(request, split));
                } else if (child instanceof Script) {
                    list.add(((Script) child).getScript(request, split));
                }
            }
            return list.stream().collect(Collectors.joining(split));
        }

        @Tag(value = "when", parentTagTypes = Choice.class, desc = "if else")
        public static class When extends Unit implements Script {
            @Override
            public String getScript(TaskInstance request, String split) {
                try {
                    if (If.test(attributes.get("test"), request.getOgnlContext(), request.getBus())) {
                        return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split));
                    }
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
                return split;
            }
        }

        @Tag(value = "otherwise", parentTagTypes = Choice.class, desc = "if else")
        public static class Otherwise extends Unit implements Script {
            @Override
            public String getScript(TaskInstance request, String split) {
                return children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, split)).collect(Collectors.joining(split));
            }
        }
    }
}