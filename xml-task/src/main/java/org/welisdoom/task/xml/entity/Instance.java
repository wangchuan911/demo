package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoom.task.xml.intf.type.Initialize;
import org.welisdoom.task.xml.intf.type.Root;

import java.util.Objects;

/**
 * @Classname Instance
 * @Description TODO
 * @Author Septem
 * @Date 19:13
 */
@Tag(value = "instance", parentTagTypes = Initialize.class)
public class Instance extends Unit implements Initialize {

    public Unit getInstance(Unit parent) {
        try {
            return SAXParserHandler.getTag(parent, attributes.get("tag")).getConstructor().newInstance().setName(this.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }

    public static Unit getInstance(Root task, Unit parent, String name) {
        return Initialization.getInstance((Task) task)
                .getChildren(Instance.class)
                .stream()
                .filter(instance -> Objects.equals(name, instance.getName()))
                .findFirst().orElse(null).getInstance(parent).setParent(parent);

    }
}
