package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.OgnlUtils;
import org.welisdoom.task.xml.intf.type.Executable;
import org.welisdoom.task.xml.intf.type.Script;

import java.util.List;
import java.util.stream.Collectors;

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
public class ForEach extends Unit implements Script {
    static String itemName = "item";

    @Override
    public String getScript(TaskInstance request, String split) {
        String collectionName = attributes.get("collection");
        String itemName = (attributes.containsKey("item")) ? attributes.get("item") : ForEach.itemName;
        Object o = OgnlUtils.getValue(collectionName, request.ognlContext, request.getBus(), Object.class);
        int size;
        String express;
        if (o.getClass().isArray()) {
            size = ((Object[]) o).length;
            express = "[%d]";
        } else {
            size = ((List) o).size();
            express = ".get(%d)";
        }
        StringBuilder sb = new StringBuilder();
        String tmp;
        if (attributes.containsKey("open")) {
            sb.append(attributes.get("open"));
            sb.append(split);
        }
        for (int i = 0; i < size; i++) {
            tmp = children.stream().filter(unit -> unit instanceof Script).map(unit -> ((Script) unit).getScript(request, " ")).collect(Collectors.joining(" ")).trim();
            sb.append(tmp.replaceAll("\\$\\{" + itemName.replace(".", "\\.").replace("@", "\\@") + "\\,", "${" + collectionName + (String.format(express, i))));
            sb.append(split);
            if (attributes.containsKey("separator")) {
                sb.append(attributes.get("separator"));
                sb.append(split);
            }
        }
        if (attributes.containsKey("close")) {
            sb.append(attributes.get("close"));
            sb.append(split);
        }
        return sb.toString();

    }
}
