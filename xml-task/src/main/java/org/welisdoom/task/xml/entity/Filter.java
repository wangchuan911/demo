package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.ObjectUtils;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname Batch
 * @Description TODO
 * @Author Septem
 * @Date 15:26
 */

@Tag(value = "filter", parentTagTypes = Iterator.class, desc = "群组，将符合条件的数据放进list")
@Attr(name = "test", desc = "条件")
public class Filter extends Unit implements Executable {

    @Override
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        try {
            if (If.test(attributes.get("test"), data.getOgnlContext(), data.getBus())) {
                List list = (List) ObjectUtils.getMapValueOrNewSafe(data.getBus(), getId(), () -> new LinkedList<>());
                list.add(preUnitResult);
            }
            return Future.succeededFuture();
        } catch (Throwable throwable) {
            return Future.failedFuture(throwable);
        }
    }

}
