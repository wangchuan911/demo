package org.welisdoom.task.xml.entity;

import com.sun.istack.NotNull;
import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.IData;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TastRequst
 * @Description TODO
 * @Author Septem
 * @Date 15:46
 */
public class TaskRequest implements IData<String, Model>, DataBaseConnectPool.IToken {
    boolean isDebugger = false;
    Map<String, Object> bus = new HashMap<>();
    OgnlContext ognlContext = (OgnlContext) Ognl.addDefaultContext(new HashMap<>(), new HashMap());
    Map<Unit, Object> cache = new LinkedHashMap<>();
    List<TaskRequest> childrenRequest = new LinkedList<>();
    TaskRequest parentRequest;

//    Object lastUnitResult;

    public synchronized void cache(Unit unit, Object o) {
        cache.put(unit, o);
    }

    public synchronized <T> T cache(Unit unit) {
        return (T) cache.get(unit);
    }

    public <T> T cache(Unit unit, ObjectUtils.IfNull<T> function) throws Throwable {
        return (T) ObjectUtils.getMapValueOrNewSafe(cache, unit, (ObjectUtils.IfNull) function);
    }

    public synchronized <T> T clearCache(Unit unit) {
        return (T) cache.remove(unit);
    }

    public TaskRequest(@NotNull String id) {
        this.id = id;
    }

    public synchronized void setBus(Unit unit, String key, Object value) {
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

    public synchronized <T> T getBus(String key) {
        return (T) bus.get(key);
    }

    public TaskRequest(@NotNull String id, Object o) {
        this.id = id;
        bus.put("$inputs", o);
    }


    public synchronized void generateData(Unit unit) {
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

//    public <T> T getLastUnitResult() {
//        return (T) lastUnitResult;
//    }

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


    public synchronized TaskRequest copy(String subId) {
        TaskRequest taskRequest = new TaskRequest(String.format("%s-%s", this.id, subId));
        taskRequest.bus.putAll(SerializationUtils.clone((HashMap) this.bus));
        taskRequest.parentRequest = this;
        this.childrenRequest.add(taskRequest);
        return taskRequest;
    }

    public TaskRequest getParentRequest() {
        return parentRequest;
    }

    public TaskRequest getRootRequest() {
        TaskRequest root = parentRequest;
        while (root.getParentRequest() != null) {
            root = root.getParentRequest();
        }
        return root;
    }

    public TaskRequest[] getChildrenRequest() {
        return childrenRequest.toArray(TaskRequest[]::new);
    }

    public Future<Void> destroy() {
        return (Future) CompositeFuture.join(cache.entrySet().stream().map(entry -> entry.getKey().destroy(this)).collect(Collectors.toList())).transform(event -> {
            bus.clear();
            return CompositeFuture.join(childrenRequest.stream().map(TaskRequest::destroy).collect(Collectors.toList())).onComplete(event1 -> childrenRequest.clear());
        });
    }
}
