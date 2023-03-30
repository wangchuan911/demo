package org.welisdoom.task.xml.entity;


import org.welisdoom.task.xml.intf.type.Script;

/**
 * @Classname SqlIf
 * @Description TODO
 * @Author Septem
 * @Date 17:57
 */
@Tag(value = "if", parentTagTypes = Script.class)
public class SqlIf extends Unit implements Script {
    @Override
    public String getScript(Object data) {
        try {
            if (If.test(attributes.get("test"), data)) {
                return String.format("%s %s", content.trim(), Script.getScript(data, children.stream().filter(unit -> unit instanceof Script).map(unit -> (Script) unit)));
            }
        } catch (OgnlException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return " ";
    }


}
