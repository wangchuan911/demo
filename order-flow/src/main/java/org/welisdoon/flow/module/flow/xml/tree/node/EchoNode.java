package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.xml.sax.Attributes;

/**
 * @Classname EchoNode
 * @Description TODO
 * @Author Septem
 * @Date 17:44
 */

@NodeType("echo")
public class EchoNode extends BaseNode {
    public EchoNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

    @Override
    public void doingRetryable(Context context) {
        System.out.println("hehe");
        super.doingRetryable(context);
    }

    @Override
    public void undoing(Context context) {
        System.out.println("hehe");
        super.undoing(context);
    }
}
