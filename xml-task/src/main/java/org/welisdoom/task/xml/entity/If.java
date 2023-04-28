package org.welisdoom.task.xml.entity;


import io.vertx.core.Promise;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;

import java.util.HashMap;

/**
 * @Classname If
 * @Description TODO
 * @Author Septem
 * @Date 17:58
 */
@Tag(value = "if", parentTagTypes = Executable.class, desc = "条件判断")
@Attr(name = "test", require = true, desc = "表达式")
public class If extends Unit implements Executable {
    /*@Override
    protected void execute(TaskRequest data) throws Throwable {
        System.out.println(data.getBus());
        System.out.println(attributes.get("test"));
        if (test(attributes.get("test"), data.getBus())) {
            System.out.println("ture");
            super.execute(data);
        } else {
            data.next(false);
        }
    }*/

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            boolean test = test(attributes.get("test"), data.getOgnlContext(), data.getBus());
            log(String.format("表达式[%s]", attributes.get("test")));
            log(String.format("参数[%s]", data.getBus().toString()));
            log(String.format("结果[%s]", test));

            if (test) {
                super.start(data, true, toNext);
            } else {
                toNext.complete(false);
            }
        } catch (OgnlException e) {
            toNext.fail(e);
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


    public static boolean test(String express, OgnlContext ognlContext, Object o) throws OgnlException {
        return (boolean) Ognl.getValue(express, ognlContext, o, boolean.class);
    }
}
