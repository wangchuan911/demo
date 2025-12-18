package org.welisdoon.metadata.prototype.handle.link.construction.sql.content;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.FormatContent;
import org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent;

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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @Classname XmlTemplateFormatContent
 * @Description TODO
 * @Author Septem
 * @Date 23:33
 */
//@Deprecated
public class XmlTemplateFormatContent extends TemplateFormatContent {
//    final static DocumentBuilderFactory DOCUMENT_BUILDER_FACTORY = DocumentBuilderFactory.newInstance();
//    final static TransformerFactory TRANSFORMER_FACTORY = TransformerFactory.newInstance();
//    final Document document;
//    String xmlResult;
//
//    public XmlTemplateFormatContent() {
//        super();
//        try {
//            DocumentBuilder db = DOCUMENT_BUILDER_FACTORY.newDocumentBuilder();
//            this.document = db.newDocument();
//            document.setXmlStandalone(true);
//        } catch (ParserConfigurationException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    public XmlTemplateFormatContent(org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent sqlContent) {
//        this();
//        sqlContent.format(this);
//    }
//
//    @Override
//    public void addColumnPart(FormatContent.Part.FormatColumn sqlAlias) {
//        xmlResult = null;
//        super.addColumnPart(sqlAlias);
//    }
//
//    @Override
//    public void addTablePart(FormatContent.Part part) {
//        xmlResult = null;
//        super.addTablePart(part);
//    }
//
//    protected void addNode(Node parent, Node relNode, String nodeName, Map<String, String> attrs, Consumer<Node> nodeConsumer) {
//        nodeConsumer.accept(addNode(parent, relNode, nodeName, attrs));
//    }
//
//    protected Node addNode(Node parent, Node relNode, String nodeName, Map<String, String> attrs) {
//        Element node = document.createElement(nodeName);
//        attrs.forEach(node::setAttribute);
//        parent.insertBefore(node, relNode);
//        return node;
//    }
//
//    protected void addNode(Node parent, String nodeName, Map<String, String> attrs, Consumer<Node> nodeConsumer) {
//        addNode(parent, null, nodeName, attrs, nodeConsumer);
//    }
//
//    protected Node addNode(Node parent, String nodeName, Map<String, String> attrs) {
//        return addNode(parent, null, nodeName, attrs);
//    }
//
//    protected Node addText(Node node, String text) {
//        return addText(node, null, text);
//    }
//
//    protected Node addText(Node node, Node relNode, String text) {
//        return addText(node, relNode, text, false);
//    }
//
//    protected Node addText(Node node, Node relNode, String text, boolean cdata) {
//        if (cdata)
//            return node.insertBefore(document.createCDATASection(text), relNode);
//        else
//            return node.insertBefore(document.createTextNode(text), relNode);
//    }
//
//    protected Node addTextTemplate(Node node, String template, Object... args) {
//        return addTextTemplate(node, null, template, args);
//    }
//
//    protected Node addTextTemplate(Node node, Node relNode, String template, Object... args) {
//        return addText(node, relNode, MessageFormat.format(template, args));
//    }
//
//
//    protected void findLeaf(Part part, List<Part> list, Predicate<Part> partPredicate) {
//        if (part instanceof VirtualPart) {
//            for (Part child : ((VirtualPart) part).getChildren()) {
//                if (child instanceof VirtualPart) {
//                    for (Part childChild : ((VirtualPart) child).getChildren()) {
//                        findLeaf(childChild, partPredicate);
//                    }
//                } else if (partPredicate.test(child)) {
//                    list.add(child);
//                }
//            }
//            return;
//        }
//        if (partPredicate.test(part)) {
//            list.add(part);
//        }
//    }
//
//  /* protected void findLeaf(List<Part> part, List<Part> list, LinkMetaType... type) {
//        for (Part part1 : part) {
//            findLeaf(part1, list, type);
//        }
//    }
//
//    protected List<Part> findLeaf(List<Part> part, LinkMetaType... type) {
//        List<Part> list = new LinkedList<>();
//        findLeaf(part, list, type);
//        return list;
//    }
//
//    protected void findLeafAndGroup(VirtualPart part, PartSplit partSplit) {
//        List<Part> list = new LinkedList<>();
//        for (Part child : part.getChildren()) {
//            if (child instanceof VirtualPart) {
//
//            }
//        }
//    }*/
//
//    protected List<Part> findLeaf(Part part, Predicate<Part> partPredicate) {
//        List<Part> list = new LinkedList<>();
//        findLeaf(part, list, partPredicate);
//        return list;
//    }
//
////    protected void groupBy(PartSplit partSplit) {
////        groupBy(this.tablePart, partSplit);
////    }
//
////    protected void groupBy(VirtualPart part1, PartSplit partSplit) {
////        List<Part> list = part1.getChildren();
////        Part first = part1.getChildren().get(0);
////        while (first instanceof VirtualPart) {
////            first = ((VirtualPart) first).getChildren().get(0);
////        }
////        List<Part> strong = new LinkedList<>();
////        List<Part> weak = new LinkedList<>();
////        List<Part> multi = new LinkedList<>();
////        for (int i = 0; i < list.size(); i++) {
////            Part part = list.get(i);
////            if (isWeakRel(list.get(i), LinkMetaType.SqlToJoinOfMultiDataRel)) {
////                multi.add(part);
////            } else if (isWeakRel(list.get(i), LinkMetaType.SqlToJoinOfWeakRel)) {
////                weak.add(part);
////            } else {
////                strong.add(part);
////            }
////        }
////        Assert.notNull(first, "没有配置任何表表");
////        partSplit.split(first, strong, weak, multi);
////    }
//
////    @FunctionalInterface
////    interface PartSplit {
////        void split(Part first, List<Part> strong, List<Part> weak, List<Part> multi);
////    }
//
//    protected void toFormatGet(final Node root, final Node currentNode, final VirtualPart part, String step, Map<String, Object> map) {
//        LeafPart part1;
//        List<LeafPart> others;
//        switch (step) {
//            case "START":
//                part1 = LeafPart.find(part, 0);
//                others = (List) findLeaf(part, part2 -> part2 instanceof LeafPart && !isRel(part2, part, LinkMetaType.SqlToJoinOfMultiDataRel));
//                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
//                this.addText(currentNode, MessageFormat.format("\nselect {0} ", others.stream().flatMap(leafPart -> ((LeafPart) leafPart).getColumns().stream()).map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).collect(Collectors.joining(" , "))));
//                Node fromNode = this.addText(currentNode, MessageFormat.format("\nfrom {0} {1}", part1.getTarget(), part1.getAlias()));
//
//                for (int i = 1; i < getTableCount(); i++) {
//                    LeafPart part3 = LeafPart.find(part, i);
//                    if (isRel(part3, part, LinkMetaType.SqlToJoinOfMultiDataRel)) continue;
//                    boolean weak = isRel(part3, part, LinkMetaType.SqlToJoinOfWeakRel);
//                    log.info("加载[强关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
//                    this.addText(currentNode, MessageFormat.format("\n{3}join {0} {1} \n  on {2}",
//                            part3.getTarget(), part3.getAlias(), String.join("\nand ", part3.getCondition()),
//                            weak ? "left " : ""
//                    ));
//                }
//
//                this.addText(currentNode, MessageFormat.format("\nwhere {0} and {1} ",
//                        part1.getColumns().stream()
//                                .findFirst().map(formatColumn -> MessageFormat.format("\n{0}.{1} = {2}", formatColumn.getAlias(), formatColumn.getTarget(), getDbParamerter(formatColumn))).orElseThrow(() -> new IllegalStateException("没有配置主键")),
//                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
//                ));
//
//                Node resultMapNode = this.addNode(root, "resultMap", Map.of("id", "objDefine"));
//                for (Part leafPart : others) {
//                    for (Part.FormatColumn column : ((LeafPart) leafPart).getColumns()) {
//                        this.addNode(resultMapNode, "result", Map.of("property", column.getField().getCode(), "column", column.getField().getCode()));
//                    }
//                }
//                int index = 0, index2;
//                for (Part child : part.getChildren()) {
//                    if (child instanceof VirtualPart && child.getType() == LinkMetaType.SqlToJoinOfMultiDataRel) {
//                        List<LeafPart> list1 = (List) findLeaf(child, part2 -> part2 instanceof LeafPart);
//                        Map<String, Object> params = new HashMap<>();
//                        StringBuilder builder = new StringBuilder("");
//                        index2 = 0;
//                        for (LeafPart leafPart : list1) {
//                            for (LeafPart leafPart1 : others) {
//                                for (String s : leafPart.getCondition()) {
//                                    int offset = s.indexOf(leafPart1.getAlias() + ".");
//                                    if (offset < 0) continue;
//                                    String valueCol = s.substring(offset, s.indexOf(" ", offset));
//                                    String valueAlias = "REL_ID_" + index++;
//                                    String argName = "arg" + index2++;
//                                    addTextTemplate(currentNode, fromNode, ",{0} as {1}", valueCol, valueAlias);
//                                    builder.append(MessageFormat.format(",{0}={1}", argName, valueAlias));
//                                    params.put(s.replace(valueCol, MessageFormat.format("#'{'{0}'}'", argName)), null);
//                                }
//                            }
//                        }
//                        if (MapUtils.isEmpty(params)) continue;
//
//                        String selectName = "select" + index++;
//                        this.addNode(currentNode, "collection", Map.of("select", selectName, "property", "", "column", builder.replace(0, 1, "{").append("}").toString()));
//
//                        this.addNode(root, "select", Map.of("id", selectName, "resultType", "string"), selectNode -> {
//                            toFormatGet(root, selectNode, (VirtualPart) child, "SUB_QUERY", params);
//                        });
//                    }
//                }
//                break;
//            case "SUB_QUERY":
//                part1 = LeafPart.findFirst(part);
//                others = (List) findLeaf(part, part2 -> part2 instanceof LeafPart && !isRel(part2, part, LinkMetaType.SqlToJoinOfMultiDataRel));
//                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
//                this.addText(currentNode,
//                        MessageFormat.format(
//                                "\nselect {0} \nfrom {1} {2}",
//                                others.stream().flatMap(leafPart -> leafPart.getColumns().stream()).map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).collect(Collectors.joining(" , ")),
//                                part1.getTarget(),
//                                part1.getAlias()
//                        )
//                );
//
//                for (Part part3 : findLeaf(part, part2 -> part2 instanceof LeafPart && part1 != part2 && !isRel(part2, part, LinkMetaType.SqlToJoinOfMultiDataRel))) {
//                    boolean weak = isRel(part3, part, LinkMetaType.SqlToJoinOfWeakRel);
//                    log.info("加载[强关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
//                    this.addText(currentNode, MessageFormat.format("\n{3}join {0} {1} \n  on {2}",
//                            part3.getTarget(), part3.getAlias(), String.join("\nand ", ((LeafPart) part3).getCondition()),
//                            weak ? "left " : ""
//                    ));
//                }
//                this.addText(currentNode,
//                        MessageFormat.format(
//                                "\nwhere {0} ",
//                                map.keySet().stream().collect(Collectors.joining(" and "))
//                        )
//                );
//        }
//    }
//
//    protected void toFormatQuery(final Node root, Node currentNode, VirtualPart part, final String step) {
//        LeafPart part1 = LeafPart.find(part, 0);
//        LeafPart part2 = LeafPart.find(part, 1);
//        switch (step) {
//            case "START":
//                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
//                this.addText(currentNode, MessageFormat.format("\nselect {0} \nfrom {1} {2} \nwhere {3}",
//                        part1.getColumns().stream().findFirst().map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).orElseThrow(() -> new IllegalStateException("没有配置主键")),
//                        part1.getTarget(), part1.getAlias(),
//                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
//                ));
//                if (part2 == null) {
//                    return;
//                }
//
//                final Node node;
//                if (isRel(part2, LinkMetaType.SqlToJoinOfWeakRel, LinkMetaType.SqlToJoinOfMultiDataRel)) {
//                    node = this.addNode(currentNode, "if", Map.of("test", part2.getColumns().stream().map(formatColumn -> {
//                        return MessageFormat.format("{0}!=null", formatColumn.getField().getCode());
//                    }).collect(Collectors.joining(" or "))));
//                } else {
//                    node = currentNode;
//                }
//
//                this.addText(node,
//                        MessageFormat.format("\nand exists (select 1 from {0} {1} ",
//                                part2.getTarget(), part2.getAlias()));
//                for (int i = 2; i < getTableCount(); i++) {
//                    LeafPart part3 = LeafPart.find(this.tablePart, i);
//                    if (!isRel(part3, LinkMetaType.SqlToJoinOfWeakRel, LinkMetaType.SqlToJoinOfMultiDataRel)) {
//                        log.info("加载[强关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
//                        this.addText(node, MessageFormat.format("\njoin {0} {1} \n  on {2}",
//                                part3.getTarget(), part3.getAlias(), String.join("\nand ", part3.getCondition())
//                        ));
//
//                        this.addIfCondition(part3, node);
//                    }
//                }
//                this.toFormatQuery(root, node, part, "WEAK_REL");
//                this.addText(node,
//                        MessageFormat.format("\nwhere {0} ",
//                                String.join("\nand ", part2.getCondition())));
//                this.addIfCondition(part2, node);
//                this.addText(node, ") \nwhere 1=1 ");
//                this.addIfCondition(part1, node);
//                break;
//            case "WEAK_REL":
//                for (Part part3 : part.getChildren()) {
//                    if (!isRel(part3, part, LinkMetaType.SqlToJoinOfWeakRel, LinkMetaType.SqlToJoinOfMultiDataRel))
//                        continue;
//                    if (part3 instanceof VirtualPart) {
//                        this.toFormatQuery(root, currentNode, (VirtualPart) part3, "WEAK_REL_DEEP");
//                    } else if (part3 instanceof LeafPart) {
//                        if (part3 == part2 || part3 == part1 || CollectionUtils.isEmpty(((LeafPart) part3).getColumns()))
//                            continue;
//                        this.addNode(currentNode, "if", Map.of("test", ((LeafPart) part3).getColumns().stream().map(formatColumn -> {
//                            return MessageFormat.format("{0}!=null", formatColumn.getField().getCode());
//                        }).collect(Collectors.joining(" or "))), ifNode -> {
//                            log.info("加载[弱关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
//                            this.addText(ifNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
//                                    part3.getTarget(), part3.getAlias(), String.join("\n and ", ((LeafPart) part3).getCondition())
//                            ));
//                            this.addIfCondition((LeafPart) part3, ifNode);
//                        });
//                    }
//                }
//                break;
//            case "WEAK_REL_DEEP":
//                Optional.of(findLeaf(part, part3 -> part3 instanceof LeafPart).stream().flatMap(leafPart -> ((LeafPart) leafPart).getColumns().stream()).map(formatColumn -> {
//                    return MessageFormat.format("{0}!=null", formatColumn.getField().getCode());
//                }).collect(Collectors.joining(" or "))).filter(StringUtils::isNotEmpty).ifPresent(test -> {
//                    this.addNode(currentNode, "if", Map.of("test", test), ifNode -> {
//                        for (Part part3 : part.getChildren()) {
//                            if (part3 instanceof VirtualPart) {
//                                this.toFormatQuery(root, currentNode, (VirtualPart) part3, "WEAK_REL_DEEP");
//                            } else if (part3 instanceof LeafPart) {
//                                if (part3 == part2 || part3 == part1) continue;
//                                log.info("加载[弱关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
//                                this.addText(ifNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
//                                        part3.getTarget(), part3.getAlias(), String.join("\n and ", ((LeafPart) part3).getCondition())
//                                ));
//                                this.addIfCondition((LeafPart) part3, ifNode);
//                            }
//                        }
//                    });
//                });
//
//                break;
//        }
//    }
//
//    protected void build(TemplateParts templatePart, Node root) {
//        switch (templatePart) {
//            case Query:
//                toFormatQuery(root, this.addNode(root, "select", Map.of("id", "query", "resultType", "string")), tablePart, "START");
////                this.addNode(root, "select", Map.of("id", "query", "resultType", "string"),
////                        queryNode -> {
////                            this.groupBy((part1, strong, weak, multi) -> {
////                                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
////                                this.addText(queryNode, MessageFormat.format("\nselect {0} \nfrom {1} {2} \nwhere {3}",
////                                        part1.getColumns().stream().findFirst().map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).orElseThrow(() -> new IllegalStateException("没有配置主键")),
////                                        part1.getTarget(), part1.getAlias(),
////                                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
////                                ));
//////                        this.getColumnParts().stream().filter(formatColumn -> part1.getAlias().equalsIgnoreCase(formatColumn.getAlias())).forEach(formatColumn -> {
//////                            this.addNode(queryNode, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
//////                                this.addTextTemplate(ifNode2, " and {0}.{1}=#'{'{2},jdbcType={3}'}'", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode(), "VARCHAR");
//////                            });
//////                        });
////                                this.addIfCondition(part1.getColumns(), part1, queryNode);
////
////
//////                                Map<Boolean, List<Part>> map = getTablePart().stream().skip(1).collect(Collectors.groupingBy(part -> isWeakRel(part, LinkMetaType.SqlToJoinOfMultiDataRel, LinkMetaType.SqlToJoinOfWeakRel)));
////
////                                List<Part> strong2 = findLeaf(strong, LinkMetaType.SqlToJoinOfStrongRel);
////                                log.info("先加载强关联的表{}个", strong2.size());
////                                strong2.stream().skip(1).findFirst().ifPresent(part2 -> {
////                                    log.info("加载[强关联]第1个表{}.{}", part2.getAlias(), part2.getTarget());
////                                    this.addText(queryNode,
////                                            MessageFormat.format("\nand exists (select 1 from {0} {1} ",
////                                                    part2.getTarget(), part2.getAlias()));
////                                    strong2.stream().skip(2).forEach(part3 -> {
////                                        log.info("加载[强关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
////                                        this.addText(queryNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
////                                                part3.getTarget(), part3.getAlias(), String.join("\nand ", part3.getCondition())
////                                        ));
////
////                                        this.addIfCondition(part3.getColumns(), part3, queryNode);
////                                    });
////
////
////                                    for (Part weak2 : weak) {
////                                        log.info("先加载[弱关联]的表");
////                                        List<Part> parts = findLeaf(weak2);
////                                        List<Part.FormatColumn> joinCols = parts.stream().flatMap(part -> part.getColumns().stream()).collect(Collectors.toList());
////
////                                        this.addNode(queryNode, "if", Map.of("test", joinCols.stream().map(formatColumn -> {
////                                            return MessageFormat.format("{0}!=null", formatColumn.getField().getCode());
////                                        }).collect(Collectors.joining(" or "))), ifNode -> {
////                                            parts.forEach(part3 -> {
////                                                        log.info("加载[弱关联]其他表{}.{}", part3.getAlias(), part3.getTarget());
////                                                        this.addText(ifNode, MessageFormat.format("\njoin {0} {1} \n  on {2}",
////                                                                part3.getTarget(), part3.getAlias(), String.join("\n and ", part3.getCondition())
////                                                        ));
//////                                                list.stream().filter(formatColumn -> part3.getAlias().equalsIgnoreCase(formatColumn.getAlias())).forEach(formatColumn -> {
//////                                                    this.addNode(ifNode, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
//////                                                        this.addTextTemplate(ifNode2, " and {0}.{1}=#'{'{2},jdbcType={3}'}'", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode(), "VARCHAR");
//////                                                    });
//////                                                });
////                                                        this.addIfCondition(joinCols, part3, ifNode);
////
////                                                    }
////                                            );
////                                        });
////                                    }
////                                    this.addText(queryNode,
////                                            MessageFormat.format("\nwhere {0} )",
////                                                    String.join("\nand ", part2.getCondition())));
////                                    this.addIfCondition(part2.getColumns(), part2, queryNode);
////                                });
////                            });
////                        });
//                break;
//            case Get:
//                this.toFormatGet(root, this.addNode(root, "select", Map.of("id", "get", "resultMap", "objDefine")), this.tablePart, "START", null);
////                this.addNode(root, "select", Map.of("id", "get", "resultMap", "objDefine"),
////                        queryNode -> {
////                            groupBy((part1, strong, weak, multi) -> {
////                                log.info("加载第一个表{}.{}", part1.getAlias(), part1.getTarget());
////                                this.addText(queryNode, MessageFormat.format("\nselect {0} \nfrom {1} {2}",
////                                        part1.getColumns().stream()
////                                                .map(formatColumn -> MessageFormat.format("\n  {0}.{1} as \"{2}\"", formatColumn.getAlias(), formatColumn.getTarget(), formatColumn.getField().getCode())).collect(Collectors.joining(" , ")),
////                                        part1.getTarget(), part1.getAlias(),
////                                        Optional.of(String.join("\nand ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse(" 1=1")
////                                ));
////
////                                findLeaf(strong).stream().skip(1).forEach(part3 -> {
////                                    log.info("加载[强关联]表{}.{}", part3.getAlias(), part3.getTarget());
////                                    this.addText(queryNode, MessageFormat.format("\n join {0} {1} \n  on {2}",
////                                            part3.getTarget(),
////                                            part3.getAlias(),
////                                            String.join("\n and ", part3.getCondition())
////                                    ));
////                                });
////                                findLeaf(weak).forEach(part3 -> {
////                                    log.info("加载[弱关联]表{}.{}", part3.getAlias(), part3.getTarget());
////                                    this.addText(queryNode, MessageFormat.format("\nleft join {0} {1} \n  on {2}",
////                                            part3.getTarget(),
////                                            part3.getAlias(),
////                                            String.join("\n and ", part3.getCondition())
////                                    ));
////                                });
////
////                                this.addText(queryNode, MessageFormat.format("\nwhere {0} and {1} ",
////                                        part1.getColumns().stream()
////                                                .findFirst().map(formatColumn -> MessageFormat.format("\n{0}.{1} = {2}", formatColumn.getAlias(), formatColumn.getTarget(), getDbParamerter(formatColumn))).orElseThrow(() -> new IllegalStateException("没有配置主键")),
////                                        Optional.of(String.join("\n and ", part1.getCondition())).filter(StringUtils::isNotEmpty).orElse("1=1")
////                                ));
////                            });
////                        });
////                /*this.addNode(root, "resultMap", Map.of("id", "object"), resultMapNode -> {
////                    getColumnParts().stream().forEach(formatColumn -> {
////                        Part part = getTableParts().stream().filter(part1 -> formatColumn.getAlias().equals(part1.getAlias())).findFirst().get();
////                        if (part.getParentType() == LinkMetaType.SqlToJoinOfMultiDataRel) {
////                            this.addNode(resultMapNode,
////                                    "collection",
////                                    Map.of("property", formatColumn.getField().getCode(),
////                                            "column", formatColumn.getField().getCode(),
////                                            "javaType", "string")
////                            );
////
////                        }
////                        this.addNode(resultMapNode,
////                                "result",
////                                Map.of("property", formatColumn.getField().getCode(),
////                                        "column", formatColumn.getField().getCode(),
////                                        "javaType", "string")
////                        );
////                    });
////                });*/
//                break;
//
//        }
//    }
//
//    protected String getDbParamerter(Part.FormatColumn formatColumn) {
//        return MessageFormat.format("#'{'{0},jdbcType={1}'}'", formatColumn.getField().getCode(), "VARCHAR");
//    }
//
//    public void build() {
//        Node root = document.createElement("mapper");
//        for (TemplateParts value : TemplateParts.values()) {
//            build(value, root);
//        }
///*
//        this.addNode(root, "select", Map.of("id", "get"),
//                queryNode -> {
//                    this.addNode(queryNode, "if", Map.of("test", ""),
//                            ifNode -> {
//
//                            });
//                });
//        this.addNode(root, "select", Map.of("id", "find"),
//                queryNode -> {
//
//                });*/
//
//        document.appendChild(root);
//        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
//            Transformer transformer = TRANSFORMER_FACTORY.newTransformer();
//            transformer.setOutputProperty(OutputKeys.INDENT, "YES");
//            transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
//            transformer.transform(new DOMSource(document), new StreamResult(byteArrayOutputStream));
//            xmlResult = byteArrayOutputStream.toString(StandardCharsets.UTF_8);
//        } catch (IOException | TransformerException e) {
//            throw new IllegalStateException(e);
//        }
//    }
//
//    public Object getValue() {
//        return xmlResult;
//    }
//
////    protected LinkedList<List<Part>> groupby(List<Part> parts) {
////        int index = -1;
////        LinkedList<List<Part>> lists = new LinkedList<>();
////        for (Part part : parts) {
////            List<Part> list;
////            if (index != part.getLevel()) {
////                index = part.getLevel();
////                list = new LinkedList<>();
////                lists.add(list);
////            } else {
////                list = lists.getLast();
////            }
////            list.add(part);
////        }
////        return lists;
////    }
//
//    protected boolean isRel(Part part, LinkMetaType... linkMetaTypes) {
//        return isRel(part, null, linkMetaTypes);
//    }
//
//    protected boolean isRel(Part part, Part end, LinkMetaType... linkMetaTypes) {
//        if (part == null) {
//            return false;
//        }
//        for (LinkMetaType linkMetaType : linkMetaTypes) {
//            if (part.getType() == linkMetaType) {
//                return true;
//            }
//        }
//        if (end == part.getParent()) {
//            return false;
//        }
//        return isRel(part.getParent(), linkMetaTypes);
//    }
//
//    protected void addIfCondition(LeafPart part3, Node node) {
//        part3.getColumns().forEach(formatColumn -> {
//            this.addNode(node, "if", Map.of("test", MessageFormat.format("{0}!=null", formatColumn.getField().getCode())), ifNode2 -> {
//                this.addTextTemplate(ifNode2, " and {0}.{1}={2}", formatColumn.getAlias(), formatColumn.getTarget(), getDbParamerter(formatColumn));
//            });
//        });
//    }
//
//    public enum TemplateParts {
//        Query, Get
//    }


    public XmlTemplateFormatContent() {
        super();
    }

    public XmlTemplateFormatContent(org.welisdoon.metadata.prototype.handle.link.construction.sql.entity.SqlContent sqlContent) {
        this();
        sqlContent.format(this);
    }

    @Override
    public void build() {
        toMapper(Map.of());
    }

    @Override
    public Object getValue() {
        return null;
    }
}
