package org.welisdoom.task.xml.entity;

import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.IData;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname TastRequst
 * @Description TODO
 * @Author Septem
 * @Date 15:46
 */
public class TaskRequest implements IData<Long, Model> {
    boolean isDebugger = false;
    Map<String, Object> bus = new HashMap<>();
    OgnlContext ognlContext = (OgnlContext) Ognl.addDefaultContext(new HashMap<>(), new HashMap());

    Object lastUnitResult;

    public void setBus(Unit unit, String key, Object value) {
        String name;
        while (StringUtils.isEmpty(name = unit.id)) {
            unit = unit.parent;
        }
        try {
            Map<String, Object> map = (Map<String, Object>) ObjectUtils.getMapValueOrNewSafe(this.bus, name, () -> new HashMap<>());
            map.put(key, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getMessage(), throwable);
        }
    }

    public Map<String, Object> getBus() {
        return bus;
    }

    public <T> T getBus(String key) {
        return (T) bus.get(key);
    }

    public TaskRequest(Object o) {
        bus.put("$inputs", o);
    }


    public void generateData(Unit unit) {
        if (StringUtils.isEmpty(unit.id)) return;
        bus.put(unit.id, new HashMap<>());
    }

    Long id;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public IData setId(Long aLong) {
        this.id = aLong;
        return this;
    }

    @Override
    public Model getDataMarker() {
        return Model.Request;
    }

    @Override
    public IData setDataMarker(Model model) {
        return null;
    }

    public Object getLastUnitResult() {
        return lastUnitResult;
    }

    public OgnlContext getOgnlContext() {
        return ognlContext;
    }
}
