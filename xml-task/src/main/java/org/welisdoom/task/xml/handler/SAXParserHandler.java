package org.welisdoom.task.xml.handler;

import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.Utils;
import org.welisdoom.task.xml.XmlTaskApplication;
import org.welisdoom.task.xml.entity.Tag;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.Unit;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname SAXParserHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:40
 */
public class SAXParserHandler extends DefaultHandler {
    Deque<Unit> units = new LinkedList<>();
    Unit current;
    int level = 0;

    protected Class<? extends Unit> getTag(Unit parent, String name) {
        return (Class<? extends Unit>) Reflections.getInstance().getTypesAnnotatedWith(Tag.class)
                .stream().filter(aClass -> matched(aClass.getAnnotation(Tag.class), parent, name)).findFirst().orElseGet(() -> Unit.class);
    }

    protected boolean matched(Tag tag, Unit parent, String name) {
        if (parent == null || parent instanceof Unit || (!tag.parentTag().equals("all") && parent.getClass().getAnnotation(Tag.class).value().equals(tag.parentTag()))) {
            return name.equals(tag.value());
        }
        return false;
    }

    @Override
    public void startDocument() throws SAXException {
        super.startDocument();
        System.out.println("SAX解析开始");
    }

    @Override
    public void endDocument() throws SAXException {
        super.endDocument();
        System.out.println("SAX解析结束");
        System.out.println(units);
    }

    @Override
    public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
        super.startElement(s, s1, s2, attributes);
        level++;
        /*print("s:" + s);
        print("s1:" + s1);*/
//        print("s2:" + s2);
        /*for (int i = 0; i < attributes.getLength(); i++) {
            String name = attributes.getQName(i);
            String value = attributes.getValue(name);
            System.out.println("属性值：" + name + "=" + value);
        }*/
        try {
            String ref = attributes.getValue("ref");
            if (ref != null) {
                current = ((Task) units.getFirst()).getInstances(ref).setParent(units.peekLast());
            } else
                current = (getTag(units.peekLast(), s2).getConstructor().newInstance())
                        .attr(attributes)
                        .setParent(units.peekLast())
                        .setName(attributes.getValue("name"));
            units.addLast(current);
        } catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
        print(current);
    }

    @Override
    public void endElement(String s, String s1, String s2) throws SAXException {
        super.endElement(s, s1, s2);
        level--;
        units.pollLast().nodeEnd();
        current = units.peekLast();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String value = new String(ch, start, length);
        if (value.trim().equals("")) {
            return;
        }
        print(value);
        current.setConntent(value);
    }

    protected void print(Object o) {
        int space = level;
        while (space-- >= 0) {
            System.out.print("*--");
        }
        System.out.print(">       ");
        System.out.println(o);

    }
}
