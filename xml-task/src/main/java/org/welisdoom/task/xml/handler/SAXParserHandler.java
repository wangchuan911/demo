package org.welisdoom.task.xml.handler;

import org.welisdoom.task.xml.annotations.Tag;
import org.welisdoom.task.xml.intf.type.BaseUnit;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Objects;

/**
 * @Classname SAXParserHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:40
 */
public class SAXParserHandler<T extends BaseUnit<?>> extends DefaultHandler {
    protected Deque<T> units = new LinkedList<>();
    protected T current;
    protected T root;
    protected int level = 0;

    public static <A extends BaseUnit> Class<A> getTag(BaseUnit parent, String name, Class<?> clazz, org.reflections.Reflections reflections) {

//        System.out.println(parent);
//        System.out.println(name);
        return (Class) reflections.getTypesAnnotatedWith(Tag.class)
                .stream()
                .filter(aClass -> clazz.isAssignableFrom(aClass))
                .filter(aClass -> Objects.equals(aClass.getAnnotation(Tag.class).value(), name))
                .filter(aClass ->
                        matched(aClass.getAnnotation(Tag.class), parent))
                .findFirst()
                .get();
    }

    public static boolean matched(Tag tag, BaseUnit parent) {
        if (parent == null
                || (Arrays.stream(tag.parentTagTypes())
                .filter(
                        aClass -> aClass.isAssignableFrom(parent.getClass())
                )
                .findFirst()
                .isPresent())) {
            return true;
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
    public void endElement(String s, String s1, String s2) throws SAXException {
        super.endElement(s, s1, s2);
        level--;
        units.pollLast();
        current = units.peekLast();
    }


    protected void print(Object o) {
        int space = level;
        int white = 10 - space;
        System.out.print(String.format("level:[%02d]", level));
        while (space-- >= 0) {
            System.out.print("######");
        }
        System.out.print(":::::>>>>>>>");
        while (white-- >= 0) {
            System.out.print("      ");
        }
        System.out.println(o);
    }

}
