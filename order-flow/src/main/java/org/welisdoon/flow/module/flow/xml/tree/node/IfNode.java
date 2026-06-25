package org.welisdoon.flow.module.flow.xml.tree.node;

import org.apache.ibatis.scripting.xmltags.OgnlCache;
import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.welisdoon.flow.module.flow.xml.tree.enums.NodeState;
import org.xml.sax.Attributes;

/**
 * @Classname IfNode
 * @Description TODO
 * @Author Septem
 * @Date 17:27
 */
@NodeType("if")
public class IfNode extends BaseNode {
    public IfNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

    protected boolean getTest(Context context) {
        return (Boolean) OgnlCache.getValue(getAttr("test"), context);
    }

    @Override
    public void doingRetryable(Context context) {
        boolean flag = getTest(context);
        context.getCurrent().setResult(new ConditionResult(flag));
        if (flag) {
            doingChild(context);
            noChildAutoDone(context);
        } else {
            done(context);
        }
    }

    @Override
    public int nextIndex(Context context) {
        ConditionResult conditionResult = context.getCurrent().getResult(ConditionResult.class);
        if (!conditionResult.flag)
            return super.nextIndex(context);

        BaseNode node;
        int index = 1;
        while ((node = getBrother(index)) instanceof IfNode) {
            if (node.getClass() == IfNode.class) {
                return index;
            }
            index++;
        }
        return index;
    }

    public static class ConditionResult {
        boolean flag;
        NodeState state;

        public ConditionResult(boolean flag) {
            this.flag = flag;
            this.state = flag ? NodeState.Done : NodeState.Skip;
        }
    }
}
