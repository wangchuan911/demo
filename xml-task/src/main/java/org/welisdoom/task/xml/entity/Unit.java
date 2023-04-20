package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.springframework.util.StringUtils;
import org.welisdoom.task.xml.consts.Model;
import org.welisdoom.task.xml.intf.type.UnitType;
import org.welisdoon.common.data.IData;
import org.xml.sax.Attributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname Unit
 * @Description TODO
 * @Author Septem
 * @Date 17:22
 */
public class Unit implements UnitType, IData<String, Model> {
    String id;
    Unit parent;
    List<Unit> children = new LinkedList<>();
    Map<String, String> attributes = new HashMap<>();

    public Unit setParent(Unit parent) {
        this.parent = parent;
        if (this.parent != null)
            this.parent.children.add(this);
        return this;
    }

    public Unit setId(String id) {
        this.id = id;
        return this;
    }

    @Override
    public Model getDataMarker() {
        return Model.Unit;
    }

    @Override
    public IData setDataMarker(Model model) {
        return this;
    }

    public String getId() {
        return id;
    }

    public Unit attr(Attributes attributes) {
        for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(name);
            /*System.out.println("属性值：" + name + "=" + value);*/
            this.attributes.put(name, value);
        }
        return this;
    }

    @Override
    public String toString() {
        return this.getClass().getCanonicalName() + "{" +
                "name='" + id + '\'' +
                '}';
    }


    public final void destroy() {
        for (Unit child : children) {
            child.destroy();
        }
        this.parent = null;
        this.children.clear();
        this.children = null;
        this.attributes.clear();
        this.attributes = null;
    }

    public void destroy(TaskRequest taskRequest) {

    }

    protected <T extends Unit> List<T> getChild(Class<T> tClass) {
        return (List) children.stream().filter(unit -> unit.getClass() == tClass).collect(Collectors.toList());
    }

    protected <T extends Unit> List<T> getChildren(Class<T> tClass) {
        List<Unit> units = new LinkedList<>();
        units.addAll(getChild(tClass));
        for (Unit child : children) {
            units.addAll(child.getChildren(tClass));
        }
        return (List) units;
    }

    protected <T extends Unit> T getParent(Class<T> tClass) {
        Class<? extends Unit> pClass = null;
        Unit target = this;
        do {
            target = target.parent;
            pClass = target.getClass();
        } while (pClass != tClass);
        return (T) target;
    }

    /*protected void execute(TaskRequest data) {
        execute(data, Object.class);
    }

    protected final void execute(TaskRequest data, Future<?> future, Class<?>... aClass) {
        Promise<Object> superPromise = data.promise;
        for (Unit child : children) {
            for (Class<?> aClass1 : aClass) {
                if (aClass1.isAssignableFrom(data.getClass())) {
                    future.compose(o -> Future.future(promise -> {
                        data.lastUnitResult = o;
                        data.setPromise(promise);
                        try {
                            child.execute(data);
                        } catch (Throwable throwable) {
                            promise.fail(throwable);
                        }
                    }));
                }

            }
        }
        future.onSuccess(superPromise::complete).onFailure(superPromise::fail);
    }

    protected final void execute(TaskRequest data, Class<?>... aClass) throws Throwable {
        execute(data, Future.succeededFuture(), aClass);
    }*/

    protected void start(TaskRequest data, Promise<Object> toNext) {
        startChildUnit(data, data.lastUnitResult, UnitType.class).onSuccess(toNext::complete).onFailure(toNext::fail);
    }


    Future<Object> startChildUnit(TaskRequest data, Object value, Class<?>... classes) {
        Future<Object> f = Future.succeededFuture(value), f1;
        for (Unit child : children) {
            if (Arrays.stream(classes).filter(aClass -> aClass.isAssignableFrom(child.getClass())).findFirst().isEmpty())
                continue;
            f = f.compose(o -> /*{
                return Future.future(promise -> {
                    data.lastUnitResult = o;
                    child.start(data, promise);
                };
            }*/prepareNextUnit(data, o, child));

        }
        return f;
    }

    protected Future<Object> prepareNextUnit(TaskRequest data, Object value, Unit unit) {
        final long[] cost = new long[1];
        return Future.future(promise -> {
            cost[0] = System.currentTimeMillis();
            data.lastUnitResult = value;
            System.out.println();
            unit.log(String.format(">>>>>>>>>>开始[%s]>>>>>>>>>>>", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)));
            unit.start(data, promise);
        }).onComplete(objectAsyncResult -> {
            unit.log(String.format("<<<<<<<<<<结束[%s][耗时:%s秒]<<<<<<<<<<<", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME), (System.currentTimeMillis() - cost[0]) / 1000.0d));
            System.out.println();
        });
    }

    protected void log(Object o) {
        printTag();
        System.out.println(o);
    }

    protected void printTag() {
        if (this.parent != null) {
            this.parent.printTag();
        }
        System.out.print("<");
        System.out.print(this.getClass().getSimpleName());
        if (!StringUtils.isEmpty(this.id)) {
            System.out.print(" id=");
            System.out.print(this.id);
        }
        System.out.print(">");
    }
}
