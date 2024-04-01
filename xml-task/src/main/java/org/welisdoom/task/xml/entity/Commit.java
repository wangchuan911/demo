package org.welisdoom.task.xml.entity;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.xml.sax.Attributes;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname Commit
 * @Description TODO
 * @Author Septem
 * @Date 11:39
 */

@Tag(value = "commit", parentTagTypes = Executable.class, desc = "提交")
@Attr(name = "batch", desc = "批次提交")
public class Commit extends Unit implements Executable {
    int batch = 1;
    AtomicInteger count = new AtomicInteger(0);

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        batch = MapUtils.getInteger(this.attributes, "batch", 1);
        return this;
    }

    @Override
    protected void start(TaskInstance data, Object preUnitResult, Promise<Object> toNext) {
        log(String.format("batch:%d,count:%d", batch, count.incrementAndGet()));
        if (batch == count.get()) {
            count.set(0);
            Optional<Transactional> optional;
            if (attributes.containsKey("link")) {
                optional = getParents(Transactional.class).stream().filter(transactional -> transactional.getId().equals(attributes.containsKey("link"))).findFirst();
            } else {
                optional = Optional.ofNullable(getParent(Transactional.class));
            }
            if (optional.isPresent()) {
                commit(optional.get(), data).onSuccess(toNext::complete).onFailure(toNext::fail);
                log("提交");
                return;
            }
        }
        toNext.complete();
    }

    protected Future<Object> commit(Transactional transactional, TaskInstance request) {
        TaskInstance root = request;
        List<Future> list = new LinkedList<>();
        while (root.parentRequest != null) {
            root = root.parentRequest;
            for (TaskInstance taskInstance : root.childrenRequest) {
                if (taskInstance.cache(transactional) == null) continue;
                list.add(transactional.commit(taskInstance));
            }
        }
        if (root == null)
            list.add(transactional.commit(request));
        return (Future) CompositeFuture.join(list);
    }
}
