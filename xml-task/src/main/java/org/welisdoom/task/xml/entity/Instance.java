package org.welisdoom.task.xml.entity;

import org.welisdoom.task.xml.handler.SAXParserHandler;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @Classname Instance
 * @Description TODO
 * @Author Septem
 * @Date 19:13
 */
@Tag(value = "instance", parentTag = Initialization.class)
public class Instance extends Unit {

    public Unit getInstance() {
        try {
            return SAXParserHandler.getTag(this.parent, attributes.get("tag")).getConstructor().newInstance().setName(this.getName());
        } catch (Throwable e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}
