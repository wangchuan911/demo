package org.welisdoon.metadata.prototype.define;


import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.resultset.DefaultResultSetHandler;
import org.apache.ibatis.executor.resultset.ResultSetHandler;
import org.apache.ibatis.plugin.*;
import org.springframework.stereotype.Component;
import org.welisdoon.metadata.prototype.dao.MetaLinkDao;

import java.sql.Statement;
import java.util.Collection;

/**
 * @Classname NodeAspect
 * @Description TODO
 * @Author wang.zhidong
 * @Date 2021/8/22 13:40
 */
@Intercepts({
        @Signature(type = ResultSetHandler.class, method = "handleResultSets", args = {Statement.class})
})
@Component
public class MetaProtoResultInterceptor implements Interceptor {


    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        boolean matched = ((invocation.getTarget() instanceof DefaultResultSetHandler) && ErrorContext.instance().toString().contains(MetaLinkDao.class.getPackageName()));

        Object o = invocation.proceed();
        if (!matched) {
            return o;
        }
        if (o instanceof MetaPrototype) {
            setState(o);
        } else if (o instanceof Collection) {
            for (Object o1 : ((Collection) o)) {
                setState(o1);
            }
        }
        return o;

    }

    void setState(Object state) {
        if (state instanceof MetaPrototype)
            ((MetaPrototype) state).setState(MetaPrototype.LifeState.Save);
        ;
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }
}
