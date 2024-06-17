package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoom.task.xml.intf.type.Script;
import org.welisdoon.common.GCUtils;
import org.welisdoon.common.LogUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @Classname ForEach
 * @Description TODO
 * @Author Septem
 * @Date 17:58
 */

@Tag(value = "foreach", parentTagTypes = Executable.class, desc = "csv文件读写")
@Attr(name = "collection", require = true)
@Attr(name = "item")
@Attr(name = "index")
@Attr(name = "open")
@Attr(name = "separator")
@Attr(name = "close")
public class ForEach extends Iterator {

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        String collectionName = attributes.get("collection");
        this.itemName = (attributes.containsKey("item")) ? attributes.get("item") : this.itemName;
        this.itemIndex = (attributes.containsKey("index")) ? attributes.get("index") : this.itemIndex;
        Object o = OgnlUtils.getValue(collectionName, data.ognlContext, data.getBus(), Object.class);
        Stream<Object> stream;
        AtomicLong index = new AtomicLong(0);
        if (o.getClass().isArray()) {
            stream = Arrays.stream((Object[]) o);
        } else {
            stream = ((List) o).stream();
        }
        return stream.map(o1 -> {
            return ForEach.super.start(data, Iterable.Item.of(index.getAndIncrement(), o1));
        }).reduce((objectFuture, objectFuture2) -> {
            return objectFuture.compose(o1 -> objectFuture2);
        }).orElse(Future.succeededFuture());
    }
}
