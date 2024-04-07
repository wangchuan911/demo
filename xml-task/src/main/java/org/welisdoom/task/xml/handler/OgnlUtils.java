package org.welisdoom.task.xml.handler;

import ognl.Ognl;
import ognl.OgnlException;
import org.apache.ibatis.builder.BuilderException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Classname OgnlUtils
 * @Description TODO
 * @Author Septem
 * @Date 16:21
 */
public class OgnlUtils {
    private static final Map<String, Object> expressionCache = new ConcurrentHashMap<>();

    public static <T> T getValue(String expression, Map context, Object root, Class<T> type) {
        try {
            return (T) Ognl.getValue(parseExpression(expression), context, root, type);
        } catch (OgnlException e) {
            throw new BuilderException("Error evaluating expression '" + expression + "'. Cause: " + e, e);
        }
    }

    private static Object parseExpression(String expression) throws OgnlException {
        Object node = expressionCache.get(expression);
        if (node == null) {
            node = Ognl.parseExpression(expression);
            expressionCache.put(expression, node);
        }
        return node;
    }
}
