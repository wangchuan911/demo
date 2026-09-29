package org.welisdoon.common;

import com.alibaba.fastjson.JSON;
import ognl.AbstractMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import ognl.OgnlException;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname OgnlUtils
 * @Description TODO
 * @Author Septem
 * @Date 16:21
 */
public class OgnlUtils {
    private static final Map<String, Object> expressionCache = new HashMap<>();

    public static <T> T getValue(String expression, Map context, Object root, Class<T> type) {
        try {
            return (T) Ognl.getValue(parseExpression(expression), context, root, type);
        } catch (Throwable e) {
            throw new IllegalArgumentException("Value:" + JSON.toJSONString(root) + "\nError evaluating expression '" + expression + "'. \nCause: " + e, e);
        }
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = ObjectUtils.getMapValueOrNewSafe(expressionCache, expression, () -> {
            return Ognl.parseExpression(expression);
        });
        return node;
    }

    public static OgnlContext getContent() {
        return (OgnlContext) Ognl.addDefaultContext(
                new HashMap<>(),
                new AbstractMemberAccess() {
                    @Override
                    public boolean isAccessible(Map context, Object target, Member member, String propertyName) {
                        int modifiers = member.getModifiers();
                        return Modifier.isPublic(modifiers);
                    }
                }, null, null, new HashMap());
    }
}
 