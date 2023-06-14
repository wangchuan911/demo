package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
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

@Tag(value = "group", parentTagTypes = Iterator.class, desc = "群组，将符合条件的数据放进list")
@Attr(name = "test", desc = "条件")
public class Group extends Unit implements Executable {

    @Override
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        try {
            if (If.test(attributes.get("test"), data.getOgnlContext(), data.getBus())) {
                List list = (List) ObjectUtils.getMapValueOrNewSafe(data.getBus(), getId(), () -> new LinkedList<>());
                list.add(preUnitResult);
            }
            toNext.complete();
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }

}
