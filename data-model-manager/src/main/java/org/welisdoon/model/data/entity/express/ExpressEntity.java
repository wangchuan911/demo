package org.welisdoon.model.data.entity.express;

import com.ql.util.express.DefaultContext;
import com.ql.util.express.ExpressRunner;
import org.apache.commons.logging.LogFactory;

import java.util.Map;

/**
 * @Classname ExpressEntity
 * @Description TODO
 * @Author Septem
 * @Date 17:08
 */
public class ExpressEntity {
    String express;

    public ExpressEntity setExpress(String express) {
        this.express = express;
        return this;
    }

    public String getExpress() {
        return express;
    }


    public <T> T run(ExpressRunner runner, Map<String, Object> params) throws Throwable {
        DefaultContext<String, Object> context = new DefaultContext<>();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            context.put(entry.getKey(), entry.getValue());
        }
        return (T) runner.execute(this.express, context, null, false, false, LogFactory.getLog(this.getClass()));

    }
}
