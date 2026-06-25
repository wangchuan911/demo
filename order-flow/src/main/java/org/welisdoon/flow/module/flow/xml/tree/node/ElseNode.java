package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.xml.sax.Attributes;

/**
 * @Classname IfNode
 * @Description TODO
 * @Author Septem
 * @Date 17:27
 */

@NodeType("else")
public class ElseNode extends ElseIfNode {
    public ElseNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

    @Override
    protected boolean getTest(Context context) {
        return true;
    }

}
