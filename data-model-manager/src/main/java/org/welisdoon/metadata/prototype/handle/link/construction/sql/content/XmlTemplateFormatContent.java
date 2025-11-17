package org.welisdoon.metadata.prototype.handle.link.construction.sql.content;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.FormatContent;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
    String xmlResult;

    public XmlTemplateFormatContent() {
        super();
        try {
            DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
            this.document = db.newDocument();
            document.setXmlStandalone(true);
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void addColumnPart(FormatContent.Part.FormatColumn sqlAlias) {
        xmlResult = null;
        super.addColumnPart(sqlAlias);
    }

    @Override
    public void addTablePart(FormatContent.Part part) {
        xmlResult = null;
        super.addTablePart(part);
    }

    protected void addNode(Node parent, String nodeName, Map<String, String> attrs, Consumer<Node> nodeConsumer) {
        Element node = document.createElement(nodeName);
        attrs.forEach(node::setAttribute);
        parent.insertBefore(node, null);
        nodeConsumer.accept(node);
    }

    protected void addNode(Node parent, String nodeName, Map<String, String> attrs) {
        addNode(parent, nodeName, attrs, node -> {
        });
    }

    protected void addText(Node node, String text) {
        addText(node, text, false);
    }

    protected void addText(Node node, String text, boolean cdata) {
        if (cdata)
            node.insertBefore(document.createCDATASection(text), null);
        else
            node.insertBefore(document.createTextNode(text), null);
    }

    protected void addTextTemplate(Node node, String template, Object... args) {
        addText(node, MessageFormat.format(template, args));
    }


    protected void findLeaf(Part part, List<Part> list) {
        if (part instanceof VirtualPart) {
            for (Part child : ((VirtualPart) part).getChildren()) {
                if (child instanceof VirtualPart) {
                    for (Part childChild : ((VirtualPart) child).getChildren()) {
                        findLeaf(childChild, list);
                    }
                } else {
                    list.add(child);
                }
            }
            return;
        }
        list.add(part);
    }

    protected void findLeaf(List<Part> part, List<Part> list) {
        for (Part part1 : part) {
            findLeaf(part1, list);
        }
    }

    protected List<Part> findLeaf(List<Part> part) {
        List<Part> list = new LinkedList<>();
        findLeaf(part, list);
        return list;
    }

    protected List<Part> findLeaf(Part part) {
        List<Part> list = new LinkedList<>();
        findLeaf(part, list);
        return list;
    }

    protected void groupBy(PartSplit partSplit) {
        groupBy(this.tablePart, partSplit);
    }

    protected void groupBy(VirtualPart part1, PartSplit partSplit) {
        List<Part> list = part1.getChildren();
        Part first = part1.getChildren().get(0);
        while (first instanceof VirtualPart) {
            first = ((VirtualPart) first).getChildren().get(0);
        }
        List<Part> strong = new LinkedList<>();
        List<Part> weak = new LinkedList<>();
        List<Part> multi = new LinkedList<>();
        for (int i = 0; i < list.size(); i++) {
            Part part = list.get(i);
            if (isWeakRel(list.get(i), LinkMetaType.SqlToJoinOfMultiDataRel)) {
                multi.add(part);
            } else if (isWeakRel(list.get(i), LinkMetaType.SqlToJoinOfWeakRel)) {
                weak.add(part);
            } else {
                strong.add(part);
            }
        }
        Assert.notNull(first, "没有配置任何表表");
        partSplit.split(first, strong, weak, multi);
    }

    @FunctionalInterface
    interface PartSplit {
        void split(Part first, List<Part> strong, List<Part> weak, List<Part> multi);
    }


    protected void build(TemplateParts templatePart, Node root) {
        switch (templatePart) {
            case Query:
                this.addNode(root, "select", Map.of("id", "query", "resultType", "string"),
                        queryNode -> {
                            this.groupBy((part1, strong, weak, multi) -> {
                                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
                                this.addText(queryNode, MessageFormat.format("\nselect {0} \nfrom {1} {2} \nwhere {3}",
                                        part1.getColumns().stream().findFirst().map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).orElseThrow(() -> new IllegalStateException("没有配置主键")),
                                        part1.getTarget(), part1.getAlias(),
                                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
                                ));
//                        this.getColumnParts().stream().filter(formatColumn -> part1.getAlias().equalsIgnoreCase(formatColumn.getAlias())).forEach(formatColumn -> {
//                            this.addNode(queryNode, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
//                                this.addTextTemplate(ifNode2, " and {0}.{1}=#'{'{2},jdbcType={3}'}'", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode(), "VARCHAR");
//                            });
//                        });
                                this.addIfCondition(part1.getColumns(), part1, queryNode);


//                                Map<Boolean, List<Part>> map = getTablePart().stream().skip(1).collect(Collectors.groupingBy(part -> isWeakRel(part, LinkMetaType.SqlToJoinOfMultiDataRel, LinkMetaType.SqlToJoinOfWeakRel)));

                                List<Part> strong2 = findLeaf(strong);
                                log.info("先加载强关联的表{}个", strong2.size());
                                strong2.stream().skip(1).findFirst().ifPresent(part2 -> {
                                    log.info("加载[强关联]第1个表{}.{}", part2.getAlias(), part2.getTarget());
                                    this.addText(queryNode,
                                            MessageFormat.format("\nand exists (select 1 from {0} {1} ",
                                                    part2.getTarget(), part2.getAlias()));
                                    strong2.stream().skip(2).forEach(part3 -> {
                                        log.info("加载[强关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
                                        this.addText(queryNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
                                                part3.getTarget(), part3.getAlias(), String.join("\nand ", part3.getCondition())
                                        ));

                                        this.addIfCondition(part3.getColumns(), part3, queryNode);
                                    });


                                    for (Part weak2 : weak) {
                                        log.info("先加载[弱关联]的表");
                                        List<Part> parts = findLeaf(weak2);
                                        List<Part.FormatColumn> joinCols = parts.stream().flatMap(part -> part.getColumns().stream()).collect(Collectors.toList());

                                        this.addNode(queryNode, "if", Map.of("test", joinCols.stream().map(formatColumn -> {
                                            return MessageFormat.format("{0}!=null", formatColumn.getField().getCode());
                                        }).collect(Collectors.joining(" or "))), ifNode -> {
                                            parts.forEach(part3 -> {
                                                        log.info("加载[弱关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
                                                        this.addText(ifNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
                                                                part3.getTarget(), part3.getAlias(), String.join("\n and ", part3.getCondition())
                                                        ));
//                                                list.stream().filter(formatColumn -> part3.getAlias().equalsIgnoreCase(formatColumn.getAlias())).forEach(formatColumn -> {
//                                                    this.addNode(ifNode, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
//                                                        this.addTextTemplate(ifNode2, " and {0}.{1}=#'{'{2},jdbcType={3}'}'", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode(), "VARCHAR");
//                                                    });
//                                                });
                                                        this.addIfCondition(joinCols, part3, ifNode);

                                                    }
                                            );
                                        });
                                    }
                                    this.addText(queryNode,
                                            MessageFormat.format("\nwhere {0} )",
                                                    String.join("\nand ", part2.getCondition())));
                                    this.addIfCondition(part2.getColumns(), part2, queryNode);
                                });
                            });
                        });
                break;
            case Get:
                this.addNode(root, "select", Map.of("id", "get", "resultMap", "object"),
                        queryNode -> {
                            groupBy((part1, strong, weak, multi) -> {
                                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
                                this.addText(queryNode, MessageFormat.format("\nselect {0} \nfrom {1} {2}",
                                        part1.getColumns().stream()
                                                .map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).collect(Collectors.joining(" , ")),
                                        part1.getTarget(), part1.getAlias(),
                                        Optional.of(String.join("\nand ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse(" 1=1")
                                ));

                                findLeaf(strong).stream().skip(1).filter(part -> !this.isWeakRel(part, LinkMetaType.SqlToJoinOfMultiDataRel)).forEach(part3 -> {
                                    log.info("加载[强关联]表{}.{}", part3.getAlias(), part3.getTarget());
                                    this.addText(queryNode, MessageFormat.format("\n{3}join {0} {1} \n  on {2}",
                                            part3.getTarget(),
                                            part3.getAlias(),
                                            String.join("\n and ", part3.getCondition()),
                                            isWeakRel(part3, LinkMetaType.SqlToJoinOfWeakRel) ? "left " : ""
                                    ));
                                });

                                this.addText(queryNode, MessageFormat.format("\nwhere {0} and {1} ",
                                        part1.getColumns().stream()
                                                .findFirst().map(formatColumn -> MessageFormat.format("\n{0}.{1} = {2}", formatColumn.getAlias(), formatColumn.getTarget(), getDbParamerter(formatColumn))).orElseThrow(() -> new IllegalStateException("没有配置主键")),
                                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
                                ));
                            });
                        });
                /*this.addNode(root, "resultMap", Map.of("id", "object"), resultMapNode -> {
                    getColumnParts().stream().forEach(formatColumn -> {
                        Part part = getTableParts().stream().filter(part1 -> formatColumn.getAlias().equals(part1.getAlias())).findFirst().get();
                        if (part.getParentType() == LinkMetaType.SqlToJoinOfMultiDataRel) {
                            this.addNode(resultMapNode,
                                    "collection",
                                    Map.of("property", formatColumn.getField().getCode(),
                                            "column", formatColumn.getField().getCode(),
                                            "javaType", "string")
                            );

                        }
                        this.addNode(resultMapNode,
                                "result",
                                Map.of("property", formatColumn.getField().getCode(),
                                        "column", formatColumn.getField().getCode(),
                                        "javaType", "string")
                        );
                    });
                });*/
                break;
        }
    }

    protected String getDbParamerter(Part.FormatColumn formatColumn) {
        return MessageFormat.format("#'{'{0},jdbcType={1}'}'", formatColumn.getField().getCode(), "VARCHAR");
    }

    public void build() {
        Node root = document.createElement("mapper");
        for (TemplateParts value : TemplateParts.values()) {
            build(value, root);
        }
/*
        this.addNode(root, "select", Map.of("id", "get"),
                queryNode -> {
                    this.addNode(queryNode, "if", Map.of("test", ""),
                            ifNode -> {

                            });
                });
        this.addNode(root, "select", Map.of("id", "find"),
                queryNode -> {

                });*/

        document.appendChild(root);
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
            transformer.transform(new DOMSource(document), new StreamResult(byteArrayOutputStream));
            xmlResult = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException | TransformerException e) {
            throw new IllegalStateException(e);
        }
    }

    public Object getValue() {
        return xmlResult;
    }

    protected LinkedList<List<Part>> groupby(List<Part> parts) {
        int index = -1;
        LinkedList<List<Part>> lists = new LinkedList<>();
        for (Part part : parts) {
            List<Part> list;
            if (index != part.getLevel()) {
                index = part.getLevel();
                list = new LinkedList<>();
                lists.add(list);
            } else {
                list = lists.getLast();
            }
            list.add(part);
        }
        return lists;
    }

    protected boolean isWeakRel(Part part, LinkMetaType... linkMetaTypes) {
        if (part == null) {
            return false;
        }
        for (LinkMetaType linkMetaType : linkMetaTypes) {
            if (part.getType() == linkMetaType) {
                return true;
            }
        }
        return isWeakRel(part.getParent(), linkMetaTypes);
    }

    protected void addIfCondition(List<Part.FormatColumn> list, Part part3, Node node) {
        list.stream().filter(formatColumn -> part3.getAlias().equalsIgnoreCase(formatColumn.getAlias())).forEach(formatColumn -> {
            this.addNode(node, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
                this.addTextTemplate(ifNode2, " and {0}.{1}={2}", formatColumn.getAlias(), formatColumn.getTarget(), getDbParamerter(formatColumn));
            });
        });
    }

    public enum TemplateParts {
        Query, Get
    }
}
