package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.xml.sax.Attributes;

/**
 * @Classname AnyNode
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
@NodeType("all")
public class AllNode extends AnyNode {

    @Override
    protected int getCount() {
        return getChildren().size();
    }

    public AllNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

}
