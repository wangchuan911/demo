package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Iterable;
import org.welisdoon.common.ObjectUtils;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname Batch
 * @Description TODO
 * @Author Septem
 * @Date 15:26
 */

@Tag(value = "batch", parentTagTypes = Iterator.class, desc = "批次,父节点数据进入列队,满足count次数，将列队传播到子节点")
@Attr(name = "count", desc = "达到count次触发子标签")
public class Batch extends Unit implements Executable {
    Map<TaskRequest, List<Iterable.Item>> integerMap = new HashMap<>();
    int count = 1;

    @Override
    public Unit attr(Attributes attributes) {
        super.attr(attributes);
        this.count = Math.max(MapUtils.getInteger(this.attributes, "count", 1), 1);
        return this;
    }

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        try {
            if (count == 1) {
                super.start(data, toNext);
                return;
            }
            List<Iterable.Item> list = ObjectUtils.getMapValueOrNewSafe(integerMap, data, LinkedList::new);
            list.add(data.getLastUnitResult());

            if (list.size() >= count) {
                data.lastUnitResult = new LinkedList<>(list);
                list.clear();
                super.start(data, toNext);
            } else {
                toNext.complete();
            }
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }
}
