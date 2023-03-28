package org.welisdoom.task.xml;

import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.welisdoom.task.xml.handler.SAXParserHandler;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @Classname XmlTaslApplication
 * @Description TODO
 * @Author Septem
 * @Date 17:06
 */
public class XmlTaskApplication {

    public static void main(String[] args) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        try {
            SAXParser sp = spf.newSAXParser();
            SAXParserHandler handler = new SAXParserHandler();
            sp.parse("src\\main\\resources\\xml\\demo.xml", handler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
