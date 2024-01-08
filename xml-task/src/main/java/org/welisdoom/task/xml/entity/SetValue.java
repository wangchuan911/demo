package org.welisdoom.task.xml.entity;

import io.vertx.core.Promise;
import ognl.Ognl;
import ognl.OgnlException;
import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
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
    protected void start(TaskRequest data, Object preUnitResult, Promise<Object> toNext) {
        Map<String, Object> map;
        try {
            map = (Map) ObjectUtils.getMapValueOrNewSafe(data.getBus(), "$values", () -> new HashMap<>());
        } catch (Throwable e) {
            toNext.fail(e);
            return;
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
                    try {
                        value = Ognl.getValue(value.toString(), data.getOgnlContext(), data.getBus(), Object.class);
                    } catch (OgnlException e) {
                        toNext.fail(e);
                        return;
                    }
                break;
            default:
                break;
        }
        map.put(attributes.get("name"), value);
        super.start(data, preUnitResult, toNext);
    }
}
