package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.xml.sax.Attributes;

/**
 * @Classname LeafNode
 * @Description TODO
 * @Author Septem
 * @Date 17:43
 */
@NodeType("doing")
public class DoingNode extends BaseNode {

    public DoingNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

    @Override
    public void doingRetryable(Context context) {
        super.doingRetryable(context);
        done(context);
    }
}
