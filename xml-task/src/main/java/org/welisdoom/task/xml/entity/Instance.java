package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoom.task.xml.intf.Copyable;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.Root;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @Classname Instance
 * @Description TODO
 * @Author Septem
 * @Date 19:13
 */
@Tag(value = "instance", parentTagTypes = Initialize.class)
public class Instance extends Unit implements Initialize {
    static Map<String, Unit> map = new HashMap<>();

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
                if (map.containsKey(refName)) {
                    unit = (Unit) ((Copyable) map.get(refName)).copy();
                } else {
                    unit = aClass.getConstructor().newInstance().setId(instance1.getId());
                    map.put(refName, unit);
                }
            } else {
                unit = aClass.getConstructor().newInstance().setId(instance1.getId());
            }
            unit.setParent(parent);
            return unit;

        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
