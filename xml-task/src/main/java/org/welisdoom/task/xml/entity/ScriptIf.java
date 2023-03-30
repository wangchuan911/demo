package org.welisdoom.task.xml.entity;


import ognl.OgnlException;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.Map;

/**
 * @Classname SqlIf
 * @Description TODO
 * @Author Septem
 * @Date 17:57
 */
@Tag(value = "if", parentTagTypes = Script.class)
public class ScriptIf extends Unit implements Script {
    @Override
    public String getScript(Map<String, Object> data) {
        try {
            if (If.test(attributes.get("test"), data)) {
                return String.format("%s%s", Script.trim(content),
                        Script.getScript(data, children.stream().filter(unit -> unit instanceof Script).map(unit -> (Script) unit)))
                        ;
            }
        } catch (OgnlException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return " ";
    }


}
