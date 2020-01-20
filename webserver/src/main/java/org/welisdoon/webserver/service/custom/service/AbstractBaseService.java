package org.welisdoon.webserver.service.custom.service;

import io.vertx.core.json.JsonObject;
import org.welisdoon.webserver.service.custom.config.CustomConst;
import org.welisdoon.webserver.service.custom.entity.TacheVO;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Map;

public abstract class AbstractBaseService {
    public abstract Object handle(int exeCode, Map map);

    @PostConstruct
    public void init() {
    }

    public static <T> T mapToObject(Map params, Class<T> type) {
        return JsonObject.mapFrom(params).mapTo(type);
    }

    Integer getOptionTache(List<String> tacheIds, final TacheVO newTacheVo) {
        if (tacheIds.stream().anyMatch(s ->
                s != null ?
                        s.trim().equals(newTacheVo.getTacheId().toString().trim())
                        : false
        )) {
            if (newTacheVo.getNextTache() >= 0)
                return getOptionTache(tacheIds, CustomConst.TACHE.TACHE_MAP.get(newTacheVo.getNextTache()));
            else
                return newTacheVo.getNextTache();
        } else {
            return newTacheVo.getTacheId();
        }
    }
}
