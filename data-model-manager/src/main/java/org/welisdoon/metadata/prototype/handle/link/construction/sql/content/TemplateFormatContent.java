package org.welisdoon.metadata.prototype.handle.link.construction.sql.content;

import org.w3c.dom.Document;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.Sql;

import javax.lang.model.element.Element;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * @Classname TemplateBuilder
 * @Description TODO
 * @Author Septem
 * @Date 16:28
 */
public abstract class TemplateFormatContent extends Sql.IFormatContent {
    final static ThreadLocal<Element> LOCAL = new InheritableThreadLocal<>();
    final static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
    private TemplateFormatContent() {

    }

    public static void build() throws ParserConfigurationException {
        DocumentBuilder db = factory.newDocumentBuilder();
        Document document = db.newDocument();
        document.setXmlStandalone(true);
    }
}
