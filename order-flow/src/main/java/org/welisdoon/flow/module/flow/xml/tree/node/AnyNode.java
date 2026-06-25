package org.welisdoon.flow.module.flow.xml.tree.node;

import org.welisdoon.flow.module.flow.xml.tree.annotation.NodeType;
import org.welisdoon.flow.module.flow.xml.tree.content.BaseNode;
import org.welisdoon.flow.module.flow.xml.tree.content.Context;
import org.welisdoon.flow.module.flow.xml.tree.enums.NodeState;
import org.xml.sax.Attributes;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * @Classname AnyNode
 * @Description TODO
 * @Author Septem
 * @Date 11:41
 */
@NodeType("any")
public class AnyNode extends BaseNode implements IParallel{

    public AnyNode(BaseNode parent, Attributes attr) {
        super(parent, attr);
    }

    @Override
    public void doingRetryable(Context context) {
        List<BaseNode> children = getChildren();
        Context[] contexts = context.copy(children.size());
        for (int i = 0; i < children.size(); i++) {
            children.get(i).doing(contexts[i]);
        }
        noChildAutoDone(context);
    }

    protected int getCount() {
        return Optional.ofNullable(getAttr("count", Integer.class)).orElse(1);
    }

    @Override
    public void done(Context context) {
        Integer count = getCount();
        context.removeCurrent(this);
        context.getCurrent().setState(NodeState.Done);
        long complate = context.getParent().getChildren().stream().filter(content1 -> content1.getPaths().isEmpty() && Objects.equals(content1.getCurrent().getNode(), this) && content1.getCurrent().getState() == NodeState.Done).count();
        if (count > complate)
            return;
        context.getParent().getChildren().removeIf(content1 -> {
            return Objects.equals(content1.getCurrent().getNode(), this);
        });
        super.done(context.getParent());
    }
}
