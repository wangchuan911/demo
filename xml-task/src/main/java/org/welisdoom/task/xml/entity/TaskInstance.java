package org.welisdoom.task.xml.entity;

import com.sun.istack.NotNull;
import io.vertx.core.Future;
import ognl.AbstractMemberAccess;
import ognl.Ognl;
import ognl.OgnlContext;
import org.apache.commons.lang3.SerializationUtils;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.connect.DataBaseConnectPool;
import org.welisdoom.task.xml.consts.MagicKey;
import org.welisdoom.task.xml.intf.type.Context;
import org.welisdoon.common.ObjectUtils;
import org.welisdoon.common.data.IData;

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname TastRequst
 * @Description TODO
 * @Author Septem
 * @Date 15:46
 */
public class TaskInstance extends Context implements DataBaseConnectPool.IToken {
    Map<Unit, Object> cache = new LinkedHashMap<>();
    List<TaskInstance> childrenRequest = new LinkedList<>();
    TaskInstance parentRequest;

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

    public TaskInstance(@NotNull String id) {
        super();
        this.id = id;
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

    public TaskInstance(@NotNull String id, Object o) {
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
        TaskInstance that = (TaskInstance) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }


    public synchronized TaskInstance copy(String subId) {
        TaskInstance taskInstance = new TaskInstance(String.format("%s-%s", this.id, subId));
        taskInstance.getBus().putAll(SerializationUtils.clone((HashMap) this.getBus()));
        taskInstance.parentRequest = this;
        this.childrenRequest.add(taskInstance);
        return taskInstance;
    }

    public TaskInstance getParentRequest() {
        return parentRequest;
    }

    public TaskInstance getRootRequest() {
        TaskInstance root = parentRequest;
        while (root.getParentRequest() != null) {
            root = root.getParentRequest();
        }
        return root;
    }

    public TaskInstance[] getChildrenRequest() {
        return childrenRequest.toArray(TaskInstance[]::new);
    }

    public Future<Void> destroy() {
        return (Future) Future.join(cache.entrySet().stream().map(entry -> entry.getKey().destroy(this)).collect(Collectors.toList())).transform(event -> {
            getBus().clear();
            return Future.join(childrenRequest.stream().map(TaskInstance::destroy).collect(Collectors.toList())).onComplete(event1 -> childrenRequest.clear());
        });
    }


    public <T> T getPrevUnitValue() {
        return (T) getBus().get(MagicKey.PREV_UNIT_RESULT);
    }

    public TaskInstance setPrevUnitValue(Object value) {
        getBus().put(MagicKey.PREV_UNIT_RESULT, value);
        return this;
    }

    public TaskInstance delPrevUnitValue() {
        getBus().remove(MagicKey.PREV_UNIT_RESULT);
        return this;
    }
}
