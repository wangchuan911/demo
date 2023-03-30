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
        try {
            System.out.println(data);
            System.out.println(attributes.get("test"));
            if (test(attributes.get("test"), data)) {
                System.out.println("ture");
                super.execute(data);
            }
        } catch (OgnlException e) {
            e.printStackTrace();
            return;
        }
    }

    /*public static void main(String[] args) throws Throwable {
        try {
            Map m = new HashMap(Map.of("test", 1));
            Map m1 = new HashMap(Map.of("test1", 1));
            Map m2 = new HashMap(Map.of("test2", 1));
            Map m3 = new HashMap(Map.of("test3", 1));
            System.out.println(test("test1==1 ", m1));
            System.out.println(test("test2=='1'", m2));
            System.out.println(test("test3=='1'", m3));
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }*/

    static OgnlContext ognlContext = (OgnlContext) Ognl.addDefaultContext(new HashMap<>(), new HashMap());

    public static boolean test(String express, Object o) throws OgnlException {
        return (boolean) Ognl.getValue(express, ognlContext, o, boolean.class);
    }
}
