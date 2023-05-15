package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

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

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        batch = MapUtils.getInteger(this.attributes, "batch", 1);
        return this;
    }

    volatile Future<Object> future = Future.succeededFuture();

    synchronized void compose(Function<Object, Future<Object>> loop) {
        future = Iterable.compose(future, loop);
    }

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            AtomicInteger count = data.cache(this, () -> new AtomicInteger(0));
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
                    if (Objects.isNull(data.parentRequest))
                        optional.get().commit(data).onSuccess(toNext::complete).onFailure(toNext::fail);
                    else
                        this.compose(o -> (Future) optional.get().commit(data).onSuccess(toNext::complete).onFailure(toNext::fail));
                    log("提交");
                    return;
                }
            }
            toNext.complete();
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }
}
