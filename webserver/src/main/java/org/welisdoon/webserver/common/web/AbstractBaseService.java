package org.welisdoon.webserver.common.web;

import io.vertx.core.json.JsonObject;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.entity.TacheVO;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseService<T> {

    public abstract Object handle(int exeCode, Map map);

    public Object handle(int exeCode, T map) {
        throw new RuntimeException();
    }

    @PostConstruct
    public void init() {
    }

    public static <K> K mapToObject(Map params, Class<K> type) {
        return JsonObject.mapFrom(params).mapTo(type);
    }

}
