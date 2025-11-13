package org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.parser;

import com.google.common.collect.ImmutableMap;
import org.apache.commons.lang3.StringUtils;
import org.reflections.Reflections;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node.Content;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node.LikeMyBatisSqlNode;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.content.xml.node.Mappers;
import org.welisdoon.web.common.ApplicationContextProvider;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @Classname SAXParserHandler
 * @Description TODO
 * @Author Septem
 * @Date 17:40
 */
public class LikeMyBatisSAXParser extends SAXParserHandler<LikeMyBatisSqlNode> {
    final Mappers mappers;

    LikeMyBatisSAXParser(Mappers mappers) {
        this.mappers = mappers;
    }

    public static Class<? extends LikeMyBatisSqlNode> getTag(LikeMyBatisSqlNode parent, String name) {
        return getTag(parent, name, LikeMyBatisSqlNode.class, ApplicationContextProvider.getBean(Reflections.class));
    }


    @Override
    public void startElement(String s, String s1, String s2, Attributes attributes) throws SAXException {
        super.startElement(s, s1, s2, attributes);
        level++;
        try {
            ImmutableMap.Builder<String, String> builder = ImmutableMap.builder();
            for (int i = 0; i < attributes.getLength(); i++) {
                String name = attributes.getQName(i);
                String value = attributes.getValue(name);
                /*System.out.println("属性值：" + name + "=" + value);*/
                builder.put(name, value);
            }

            current = (getTag(units.peekLast(), s2)
                    .getConstructor(LikeMyBatisSqlNode.class, Map.class)
                    .newInstance(units.peekLast() != null ? units.peekLast() : mappers, builder.build()));

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
        new Content(current, Map.of()).setContent(value);
    }


    public static void load(InputStream inputStream, Mappers mappers) throws ParserConfigurationException, SAXException, IOException {
        LikeMyBatisSAXParser handler = new LikeMyBatisSAXParser(mappers);
        getSaxParser().parse(inputStream, handler);
    }

    protected static SAXParser getSaxParser() throws ParserConfigurationException, SAXException {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        return spf.newSAXParser();
    }

}
