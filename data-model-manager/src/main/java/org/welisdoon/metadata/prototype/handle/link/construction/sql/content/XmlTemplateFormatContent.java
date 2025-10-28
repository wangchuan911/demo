package org.welisdoon.metadata.prototype.handle.link.construction.sql.content;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.function.Consumer;

/**
 * @Classname XmlTemplateFormatContent
 * @Description TODO
 * @Author Septem
 * @Date 23:33
 */
public class XmlTemplateFormatContent extends TemplateFormatContent {
    final static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
    final static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
    final Document document;
    long objectId;
    String xmlResult;

    public XmlTemplateFormatContent(long objectId) {
        super();
        try {
            DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            this.document = db.newDocument();
            document.setXmlStandalone(true);
            this.objectId = objectId;
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addColumnPart(FormatColumn sqlAlias) {
        xmlResult = null;
        super.addColumnPart(sqlAlias);
    }

    @Override
    public void addTablePart(Part part) {
        xmlResult = null;
        super.addTablePart(part);
    }

    protected void addNode(Node parent, String nodeName, Map<String, String> attrs, Consumer<Node> nodeConsumer) {
        Element node = document.createElement(nodeName);
        attrs.forEach(node::setAttribute);
        parent.insertBefore(node, null);
        nodeConsumer.accept(node);

    }

    protected void addText(Node node, String text) {
        node.insertBefore(document.createCDATASection(text), null);
    }


    public void build() {
        Node root = document.createElement("mapper");
        this.addNode(root, "select", Map.of("id", "query"),
                queryNode -> {

                });
        this.addNode(root, "select", Map.of("id", "get"),
                queryNode -> {
                    this.addNode(queryNode, "if", Map.of("test", ""),
                            ifNode -> {

                            });
                });
        this.addNode(root, "select", Map.of("id", "find"),
                queryNode -> {

                });

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.transform(new DOMSource(document), new StreamResult(byteArrayOutputStream));
            xmlResult = byteArrayOutputStream.toString("utf-8");
        } catch (IOException | TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    public String getXmlResult() {
        return xmlResult;
    }
}
