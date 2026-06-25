package org.welisdoon.flow.module.flow.xml.tree.handler;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.springframework.util.Assert;
import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.welisdoon.flow.module.flow.xml.tree.node.FlowNode;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

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
public final class SAXParserHandler extends DefaultHandler2 {
    Reflections reflections;
    Deque<BaseNode> units = new LinkedList<>();
    BaseNode current;
    BaseNode root;
    int level = 0;

    public SAXParserHandler(Reflections reflections) {
        this.reflections = reflections;
    }

    public Class<? extends BaseNode> getTag(String name) {

//        System.out.println(parent);
//        System.out.println(name);
        return (Class<? extends BaseNode>) reflections.getTypesAnnotatedWith(NodeType.class)
                .stream()
                .filter(aClass -> aClass.getAnnotation(NodeType.class).value().equalsIgnoreCase(name))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("not found:<" + name + "/>"));
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
            current = (getTag(s2)
                    .getConstructor(BaseNode.class, Attributes.class)
                    .newInstance(units.peekLast(), attributes));
            Assert.isTrue(StringUtils.isNotEmpty(current.getId()), s2 + ":id不能为空");
            units.addLast(current);
        } catch (Exception e) {
            System.out.println(s2);
            throw new SAXException(e.getMessage(), e);
        }
        root = root != null ? root : current;
        print(current);
    }

    @Override
    public void endElement(String s, String s1, String s2) throws SAXException {
        super.endElement(s, s1, s2);
        level--;
        units.pollLast();
        current = units.peekLast();
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        super.characters(ch, start, length);
        String value = new String(ch, start, length);
        if (StringUtils.isAllBlank(value)) {
            return;
        }
//        print(value);
//        new ContentNode().setContent(value).setParent(current);
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

    public static FlowNode loadTask(String uri, Reflections reflections, boolean cache) throws ParserConfigurationException, SAXException, IOException {
        SAXParserHandler handler = new SAXParserHandler(reflections);
        getSaxParser().parse(uri, handler);
        if (cache)
            TEMPLATE.put(handler.root.getId(), (FlowNode) handler.root);
        return (FlowNode) handler.root;
    }

    public static FlowNode loadTask(File file, Reflections reflections, boolean cache) throws ParserConfigurationException, SAXException, IOException {

        SAXParserHandler handler = new SAXParserHandler(reflections);
        getSaxParser().parse(file, handler);
        if (cache)
            TEMPLATE.put(handler.root.getId(), (FlowNode) handler.root);
        return (FlowNode) handler.root;
    }

    public static FlowNode loadTask(InputStream inputStream, Reflections reflections, boolean cache) throws ParserConfigurationException, SAXException, IOException {

        SAXParserHandler handler = new SAXParserHandler(reflections);
        getSaxParser().parse(inputStream, handler);
        if (cache)
            TEMPLATE.put(handler.root.getId(), (FlowNode) handler.root);
        return (FlowNode) handler.root;
    }

    protected static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        return spf.newSAXParser();
    }


    public static void main(String[] args) throws IOException, SAXException, ParserConfigurationException {
        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setUrls(ClasspathHelper.forPackage("org.welisdoon.flow.module.flow.xml"));
        configurationBuilder.setInputsFilter(s -> s.toLowerCase().endsWith(".class") || s.toLowerCase().endsWith(".java"));
        FlowNode node = loadTask(new File("D:\\Workspaces\\WelisdoonProject\\java\\web-server\\order-flow\\src\\main\\resources\\demo\\xml-flow.xml"), new Reflections(configurationBuilder), true);
        Context context = new Context();
        node.doing(context);

    }

    static Map<String, FlowNode> TEMPLATE = new HashMap<>();

    public static FlowNode getFlow(String key) {
        return TEMPLATE.get(key);
    }


    public static <T extends BaseNode> T findNode(BaseNode node, String id) {
        if (Objects.equals(node.getId(), id)) {
            return (T) node;
        }
        if (CollectionUtils.isEmpty(node.getChildren())) {
            return null;
        }
        BaseNode _node = null;
        for (BaseNode child : node.getChildren()) {
            _node = findNode(child, id);
            if (_node != null) {
                break;
            }
        }
        return (T) _node;
    }
}
