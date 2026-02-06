package org.welisdoom.task.xml.intf.type;

import ognl.AbstractMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import org.welisdoon.common.LayersMap;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname Context
 * @Description TODO
 * @Author Septem
 * @Date 8:59
 */
public abstract class Context {

    final LayersMap<String, Object> bus;
    final OgnlContext ognlContext;

    public Context(OgnlContext ognlContext, LayersMap<String, Object> bus) {
        this.bus = bus;
        this.ognlContext = ognlContext;
    }

    public Context() {
        this(new LayersMap<>(new HashMap<>()));
    }

    public Context(LayersMap<String, Object> bus) {
        this((OgnlContext) Ognl.addDefaultContext(
                new HashMap<>(),
                new AbstractMemberAccess() {
                    @Override
                    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
                        int modifiers = member.getModifiers();
                        return Modifier.isPublic(modifiers);
                    }
                }, null, null, new HashMap()),
                bus);
    }

    public OgnlContext getOgnlContext() {
        return ognlContext;
    }

    public LayersMap<String, Object> getBus() {
        return bus;
    }
}
