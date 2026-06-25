package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.xml.sax.Attributes;

/**
 * @Classname RootNode
 * @Description TODO
 * @Author Septem
 * @Date 18:00
 */
@NodeType("flow")
public class FlowNode extends BaseNode implements IRoot {

    public FlowNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }


    @Override
    public String getId() {
        return getAttr("id");
    }


}
