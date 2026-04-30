package org.welisdoom.task.xml.handler;

import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.builder.BuilderException;
import org.welisdoom.task.xml.intf.type.Context;
import org.welisdoon.common.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
            throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    public static <T> T getValue(String expression, Context context, Class<T> type) {
        return (T) getValue(expression, context.getOgnlContext(), context.getBus(), type);
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = ObjectUtils.getMapValueOrNewSafe(expressionCache, expression, () -> {
            return Ognl.parseExpression(expression);
        });
        return node;
    }
}
