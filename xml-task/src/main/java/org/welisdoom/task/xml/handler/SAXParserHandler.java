package org.welisdoom.task.xml.handler;

import org.welisdoom.task.xml.entity.Instance;
import org.welisdoom.task.xml.entity.Tag;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.Unit;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @Classname SAXParserHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:40
 */
public class SAXParserHandler extends DefaultHandler {
    Deque<Unit> units = new LinkedList<>();
    Unit current;
    Unit root;
    int level = 0;

    public static Class<? extends Unit> getTag(Unit parent, String name) {
        return (Class<? extends Unit>) Reflections.getInstance().getTypesAnnotatedWith(Tag.class)
                .stream()
                .filter(aClass ->
                        matched(aClass.getAnnotation(Tag.class), parent, name))
                .findFirst()
                .orElseGet(() -> Unit.class);
    }

    public static boolean matched(Tag tag, Unit parent, String name) {
        if (parent == null
                || parent instanceof Unit
                || (Arrays.stream(tag.parentTag())
                .filter(aClass -> aClass == Unit.class)
                .findFirst()
                .isEmpty()
                && Arrays.stream(tag.parentTag())
                .filter(aClass -> parent.getClass() == aClass)
                .findFirst()
                .isPresent())) {
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
                current = Instance.getInstance((Task) units.getFirst(), ref)
                        .setParent(units.peekLast());
            } else
                current = (getTag(units.peekLast(), s2)
                        .getConstructor()
                        .newInstance())
                        .attr(attributes)
                        .setParent(units.peekLast())
                        .setName(attributes.getValue("name"));
            units.addLast(current);
        } catch (Exception e) {
            throw new SAXException(e.getMessage(), e);
        }
        root = root != null ? root : current;
        print(current);
    }

    @Override
    public void endElement(String s, String s1, String s2) throws SAXException {
        super.endElement(s, s1, s2);
        level--;
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
        current.setContent(value);
    }

    protected void print(Object o) {
        int space = level;
        while (space-- >= 0) {
            System.out.print("*--");
        }
        System.out.print(">       ");
        System.out.println(o);
    }

    public static Unit loadTask(String uri) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParserHandler handler;
        SAXParser sp = spf.newSAXParser();
        handler = new SAXParserHandler();
        sp.parse(uri, handler);
        return handler.root;
    }

    public static Unit loadTask(File file) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParserHandler handler;
        SAXParser sp = spf.newSAXParser();
        handler = new SAXParserHandler();
        sp.parse(file, handler);
        return handler.root;
    }

    public static Unit loadTask(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        SAXParserHandler handler;
        SAXParser sp = spf.newSAXParser();
        handler = new SAXParserHandler();
        sp.parse(inputStream, handler);
        return handler.root;
    }

}
