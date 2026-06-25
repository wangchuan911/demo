package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.xml.sax.Attributes;

/**
 * @Classname IfNode
 * @Description TODO
 * @Author Septem
 * @Date 17:27
 */

@NodeType("else-if")
public class ElseIfNode extends IfNode {

    public ElseIfNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }
}
