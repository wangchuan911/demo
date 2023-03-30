package org.welisdoom.task.xml.entity;


import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.welisdoom.task.xml.intf.type.Executable;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname If
 * @Description TODO
 * @Author Septem
 * @Date 17:58
 */
@Tag(value = "if", parentTagTypes = Executable.class)
public class If extends Unit implements Executable {
    @Override
    protected void execute(Map<String, Object> data) {

        OgnlContext context = (OgnlContext) Ognl.addDefaultContext(data, new HashMap());
        try {
            if ((Boolean) Ognl.getValue("", context, context.getRoot())) {
                super.execute(data);
            }
        } catch (OgnlException e) {
            e.printStackTrace();
            return;
        }
    }
}
