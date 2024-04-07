package org.welisdoom.task.xml.entity;

import io.vertx.core.Future;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoon.common.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
    protected Future<Object> start(TaskInstance data, Object preUnitResult) {
        Map<String, Object> map;
        try {
            map = (Map) ObjectUtils.getMapValueOrNewSafe(data.getBus(), "$values", () -> new HashMap<>());
        } catch (Throwable e) {
            return Future.failedFuture(e);
        }
        Object value;
        if (attributes.containsKey("value")) {
            value = textFormat(data, attributes.get("value"));
        } else if (getChild(Content.class).stream().filter(content -> !StringUtils.isBlank(content.getContent())).count() > 0) {
            value = textFormat(data, getChild(Content.class).stream().map(Content::getContent).collect(Collectors.joining(" ")));
        } else
            value = preUnitResult;
        String style = textFormat(data, attributes.get("style"));
        switch (style) {
            case "object":
                if (value instanceof String)
                    value = OgnlUtils.getValue(value.toString(), data.getOgnlContext(), data.getBus(), Object.class);
                break;
            default:
                break;
        }
        map.put(attributes.get("name"), value);
        return super.start(data, preUnitResult);
    }
}
