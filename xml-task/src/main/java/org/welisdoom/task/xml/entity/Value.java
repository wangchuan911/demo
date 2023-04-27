package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import org.apache.commons.collections4.MapUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoon.common.ObjectUtils;
import org.xml.sax.Attributes;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Values
 * @Description TODO
 * @Author Septem
 * @Date 19:24
 */
@Tag(value = "value", parentTagTypes = Initialize.class, desc = "初始化值")
@Attr(name = "id", desc = "唯一标识", require = true)
@Attr(name = "value", desc = "值", require = true)
public class Value extends Unit implements Initialize {

    @Override
    public Unit attr(Attributes attributes) {
        return super.attr(attributes);
    }

    public String getValue() {
        return MapUtils.getString(attributes, "value", getChild(Content.class).stream().findFirst().orElse(new Content().setContent("")).getContent());
    }

    /*public static String getValue(Task task, String name) {
        return Initialization.getInstance(task)
                .getChildren(Value.class)
                .stream()
                .filter(value -> Objects.equals(name, value.getId()))
                .findFirst().orElse(new Value()).getValue();

    }*/

    @Override
    protected void start(TaskRequest data, Promise<Object> toNext) {
        try {
            HashMap map = (HashMap) ObjectUtils.getMapValueOrNewSafe(data.getBus(), "$values", HashMap::new);
            String value = textFormat(data, getValue());
            log("value:" + value);
            map.put(this.id, value);
            toNext.complete();
        } catch (Throwable throwable) {
            toNext.fail(throwable);
        }
    }
}
