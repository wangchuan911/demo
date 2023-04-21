package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoon.common.ObjectUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * @Classname SetValue
 * @Description TODO
 * @Author Septem
 * @Date 9:42
 */

@Tag(value = "set-value", parentTagTypes = {Executable.class}, desc = "将前一个unit的结果保存到全局中")
@Attr(name = "name", desc = "设置的变量名", require = true)
public class SetValue extends Unit {
    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        Map<String, Object> map;
        try {
            map = (Map) ObjectUtils.getMapValueOrNewSafe(data.getBus(), "$values", () -> new HashMap<>());
        } catch (Throwable e) {
            toNext.fail(e);
            return;
        }
        map.put(attributes.get("name"), data.getLastUnitResult());
        super.start(data, toNext);
    }
}
