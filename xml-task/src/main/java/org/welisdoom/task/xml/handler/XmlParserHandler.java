package org.welisdoom.task.xml.handler;

import org.apache.commons.lang3.StringUtils;
import org.welisdoom.task.xml.dao.ConfigDao;
import org.welisdoom.task.xml.entity.Content;
import org.welisdoom.task.xml.entity.Instance;
import org.welisdoom.task.xml.entity.Task;
import org.welisdoom.task.xml.entity.Unit;
import org.welisdoom.task.xml.intf.ApplicationContextProvider;
import org.welisdoom.task.xml.intf.type.Root;
import org.welisdoon.common.ObjectUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * @Classname SAXParserHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:40
 */
public class XmlParserHandler extends SAXParserHandler<Unit> {
    public static final Map<String, Task> TASK_MAP = new HashMap<>();

    public static Class<? extends Unit> getTag(Unit parent, String name) {
        return getTag(parent, name, Unit.class, Reflections.getInstance());
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
                current = Instance.getInstance((Root) units.getFirst(), units.peekLast(), ref);
            } else
                current = (getTag(units.peekLast(), s2)
                        .getConstructor()
                        .newInstance())
                        .attr(attributes)
                        .setParent(units.peekLast())
                        .setId(attributes.getValue("id"));
            units.addLast(current);
        } catch (Exception e) {
            System.out.println(s2);
            throw new SAXException(e.getMessage(), e);
        }
        root = root != null ? root : current;
        print(current);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String value = new String(ch, start, length);
        if (StringUtils.isAllBlank(value)) {
            return;
        }
//        print(value);
        new Content().setContent(value).setParent(current);
    }

    public static Task loadTask(String uri) throws ParserConfigurationException, SAXException, IOException {

        XmlParserHandler handler = new XmlParserHandler();
        getSaxParser().parse(uri, handler);
        Task task = (Task) handler.root;
        TASK_MAP.put(uri, task);
        return task;
    }

    public static Task loadTask(File file) throws Throwable {
        return ObjectUtils.getMapValueOrNewSafe(TASK_MAP, file.getAbsolutePath(), () -> {
            XmlParserHandler handler = new XmlParserHandler();
            getSaxParser().parse(file, handler);
            return (Task) handler.root;
        });
    }

    public static Task loadTask(InputStream inputStream) throws ParserConfigurationException, SAXException, IOException {
        XmlParserHandler handler = new XmlParserHandler();
        getSaxParser().parse(inputStream, handler);
        return (Task) handler.root;
    }

    public static Task loadTask(Long id) throws Throwable {
        return ObjectUtils.getMapValueOrNewSafe(TASK_MAP, "DB:" + id, () -> loadTask(new ByteArrayInputStream(ApplicationContextProvider.getApplicationContext().getBean(ConfigDao.class).getTaskXML(id).getBytes("utf-8"))));
    }

    protected static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        return spf.newSAXParser();
    }

}
