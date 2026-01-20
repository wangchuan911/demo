package org.welisdoom.task.xml.entity;

import com.sun.istack.NotNull;
import io.vertx.core.Future;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.consts.MagicKey;
import org.welisdoom.task.xml.intf.ISession;
import org.welisdoom.task.xml.intf.type.Context;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.IData;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @Classname TastRequst
 * @Description TODO
 * @Author Septem
 * @Date 15:46
 */
public class TaskSession extends Context implements DataBaseConnectPool.IToken, ISession {
    Map<Unit, Object> cache = new LinkedHashMap<>();
    List<TaskSession> childrenSession = new LinkedList<>();
    TaskSession parentRequest;
    Object value;

//    Object lastUnitResult;


    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

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

    public TaskSession(@NotNull String id) {
        super();
        this.id = id;
    }

    public synchronized void setResult(Unit unit, Object result) {
        if (unit.id != null && result != null) {
            setBus(unit, unit.id, result);
        }
        setValue(result);
    }

    public synchronized void setBus(Unit unit, String key, Object value) {
        String name;
        while (StringUtils.isEmpty(name = unit.id)) {
            unit = unit.parent;
        }
        try {
            Map<String, Object> map = (Map<String, Object>) ObjectUtils.getMapValueOrNewSafe(this.getBus(), name, () -> new HashMap<>());
            map.put(key, value);
        } catch (Throwable throwable) {
            throw new RuntimeException(throwable.getMessage(), throwable);
        }
    }

    public synchronized <T> T getBus(String key) {
        return (T) getBus().get(key);
    }

    public TaskSession(@NotNull String id, Object o) {
        this(id);
        getBus().put(MagicKey.INPUTS, o);
    }


    public synchronized void generateData(Unit unit) {
        if (StringUtils.isEmpty(unit.id)) return;
        getBus().put(unit.id, new HashMap<>());
    }

    String id;

    public String getId() {
        return id;
    }

    public IData setId(String aLong) {
        this.id = aLong;
        return (IData) this;
    }


//    public <T> T getLastUnitResult() {
//        return (T) lastUnitResult;
//    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaskSession that = (TaskSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    public synchronized List<TaskSession> newSession(int count, Function<Integer, String> value) {
        if (count > this.childrenSession.size()) {
            return childrenSession.subList(0, count);
        }
        List<TaskSession> taskSessions = new LinkedList<>();
        taskSessions.addAll(this.childrenSession);
        for (int i = this.childrenSession.size(); i < count; i++) {
            TaskSession taskSession = new TaskSession(String.format("%s-%s", this.id, value.apply(i)));
            taskSession.getBus().putAll(SerializationUtils.clone((HashMap) this.getBus()));
            taskSession.parentRequest = this;
            taskSession.setValue(this.getValue());
            this.childrenSession.add(taskSession);
        }
        return List.copyOf(taskSessions);
    }

    public TaskSession getParentRequest() {
        return parentRequest;
    }

    public TaskSession getRootRequest() {
        TaskSession root = parentRequest;
        while (root.getParentRequest() != null) {
            root = root.getParentRequest();
        }
        return root;
    }

    public TaskSession[] getChildrenRequest() {
        return childrenSession.toArray(TaskSession[]::new);
    }

    @Deprecated
    public Future<Void> destroy() {
        return (Future) Future.join(cache.entrySet().stream().map(entry -> entry.getKey().destroy(this)).collect(Collectors.toList())).transform(event -> {
            getBus().clear();
            return Future.join(childrenSession.stream().map(TaskSession::destroy).collect(Collectors.toList())).onComplete(event1 -> childrenSession.clear());
        });
    }

    public void destroySync() {
        for (Map.Entry<Unit, Object> entry : cache.entrySet()) {
            entry.getKey().destroySync(this);
            getBus().clear();
            for (TaskSession taskSession : childrenSession) {
                taskSession.destroySync();
            }
            childrenSession.clear();
        }
    }


    public <T> T getPrevUnitValue() {
        return (T) getBus().get(MagicKey.PREV_UNIT_RESULT);
    }

    public TaskSession setPrevUnitValue(Object value) {
        getBus().put(MagicKey.PREV_UNIT_RESULT, value);
        return this;
    }

    public TaskSession delPrevUnitValue() {
        getBus().remove(MagicKey.PREV_UNIT_RESULT);
        return this;
    }

    public static class SubTaskSession extends TaskSession {

        public SubTaskSession(String id) {
            super(id);
        }

        public SubTaskSession(String id, Object o) {
            super(id, o);
        }
    }
}
