package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Attr;
import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoon.common.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Instance
 * @Description TODO
 * @Author Septem
 * @Date 19:13
 */
@Tag(value = "instance", parentTagTypes = Initialize.class, desc = "实例化标签,此标签实例化，基本信息，其他标签通过ref引用,")
@Attr(name = "id", desc = "唯一标识", require = true)
public class Instance extends Unit implements Initialize {
    static Map<Root, Map<String, Unit>> rootMapMap = new HashMap<>();

    public Unit getInstance(Unit parent) {
        try {
            return getInstanceType(parent).getConstructor().newInstance().setId(this.getId());
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public Class<? extends Unit> getInstanceType(Unit parent) {
        try {
            return SAXParserHandler.getTag(parent, attributes.get("tag"));
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Unit getInstance(Root task, Unit parent, String refName) {
        try {
            Instance instance1 = Initialization.getInstance((Task) task)
                    .getChildren(Instance.class)
                    .stream()
                    .filter(instance -> Objects.equals(refName, instance.getId()))
                    .findFirst().orElse(null);

            Class<? extends Unit> aClass = instance1.getInstanceType(parent);

            Unit unit;
            if (Copyable.class.isAssignableFrom(aClass)) {
                Map<String, Unit> map = ObjectUtils.getMapValueOrNewSafe(rootMapMap, task, HashMap::new);
                if (map.containsKey(refName)) {
                    unit = (Unit) ((Copyable) map.get(refName)).copy();
                } else {
                    unit = aClass.getConstructor().newInstance().setId(instance1.getId());
                    copyChildren(unit, instance1);
                    map.put(refName, unit);
                }
            } else {
                unit = aClass.getConstructor().newInstance().setId(instance1.getId());
                copyChildren(unit, instance1);
            }
            unit.setParent(parent);
            return unit;

        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    protected static void copyChildren(Unit unit, Instance instance) {
        instance.attributes.entrySet().stream().filter(stringStringEntry -> !stringStringEntry.getKey().equals("tag")).forEach(stringStringEntry -> {
            unit.attributes.put(stringStringEntry.getKey(), stringStringEntry.getValue());
        });
        for (Unit child : instance.children) {
            if (child instanceof Copyable)
                unit.children.add((Unit) ((Copyable) child).copy());
        }
    }
}
