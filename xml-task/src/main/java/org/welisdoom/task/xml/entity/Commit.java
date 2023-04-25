package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Stream;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Classname Commit
 * @Description TODO
 * @Author Septem
 * @Date 11:39
 */

@Tag(value = "commit", parentTagTypes = Stream.class, desc = "提交")
@Attr(name = "batch", desc = "字段信息")
public class Commit extends Unit implements Executable {
    int batch = 1;
    AtomicInteger count = new AtomicInteger(0);

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        return this;
    }

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        if (batch == count.incrementAndGet()) {
            Optional<Transactional> optional;
            if (attributes.containsKey("link")) {
                optional = getParents(Transactional.class).stream().filter(transactional -> transactional.getId().equals(attributes.containsKey("link"))).findFirst();
            } else {
                optional = Optional.ofNullable(getParent(Transactional.class));
            }
            if (optional.isPresent()) {
                Transactional.commit(data).onSuccess(toNext::complete).onFailure(toNext::fail);
                return;
            }
        }
        toNext.complete();
    }
}
