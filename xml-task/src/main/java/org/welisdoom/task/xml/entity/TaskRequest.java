package org.welisdoom.task.xml.entity;

import com.sun.istack.NotNull;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.IData;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname TastRequst
 * @Description TODO
 * @Author Septem
 * @Date 15:46
 */
public class TaskRequest implements IData<String, Model> {
    boolean isDebugger = false;
    Map<String, Object> bus = new HashMap<>();
    OgnlContext ognlContext = (OgnlContext) Ognl.addDefaultContext(new HashMap<>(), new HashMap());

    Object lastUnitResult;

    public TaskRequest(@NotNull String id) {
        this.id = id;
    }

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

    public TaskRequest(@NotNull String id, Object o) {
        this.id = id;
        bus.put("$inputs", o);
    }


    public void generateData(Unit unit) {
        if (StringUtils.isEmpty(unit.id)) return;
        bus.put(unit.id, new HashMap<>());
    }

    String id;

    @Override
    public String getId() {
        return id;
    }

    @Override
    public IData setId(String aLong) {
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

    public <T> T getLastUnitResult() {
        return (T) lastUnitResult;
    }

    public OgnlContext getOgnlContext() {
        return ognlContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskRequest that = (TaskRequest) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
