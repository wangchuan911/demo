package org.welisdoon.flow.module.flow.xml.tree.content;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.welisdoon.flow.module.flow.xml.tree.enums.NodeState;
import org.welisdoon.flow.module.flow.xml.tree.node.AnyNode;
import org.welisdoon.flow.module.flow.xml.tree.node.IParallel;
import org.welisdoon.flow.module.flow.xml.tree.node.IRoot;
import org.xml.sax.Attributes;

import java.util.*;

/**
 * @Classname Node
 * @Description TODO
 * @Author Septem
 * @Date 16:56
 */
public abstract class BaseNode {
    final BaseNode parent;
    List<BaseNode> children = List.of();
    final Map<String, String> attr;

    public BaseNode(BaseNode parent, Attributes attr) {
        this.parent = parent;
        if (this.parent != null) {
            int len = parent.children.size();
            BaseNode[] nodes = new BaseNode[len + 1];
            nodes = parent.children.toArray(nodes);
            nodes[len] = this;
            parent.children = List.of(nodes);
        }
        Map.Entry<String, String>[] entries = new Map.Entry[attr.getLength()];
        for (int i = 0; i < attr.getLength(); i++) {
            entries[i] = Map.entry(attr.getQName(i), attr.getValue(i));
        }
        this.attr = Map.ofEntries(entries);
    }

    public BaseNode getParent() {
        return parent;
    }

    public List<BaseNode> getChildren() {
        return children;
    }

    public void doingChild(Context context) {
        if (CollectionUtils.isEmpty(children)) return;
        children.get(0).doing(context);
    }

    public void init(Context context) {
        context.addCurrent(this);
        setCurrentState(context, NodeState.Waiting);
    }

    public final void doing(Context context) {
        init(context);
        doingRetryable(context);
    }

    public void doingRetryable(Context context) {
        doingChild(context);
        noChildAutoDone(context);
    }

    protected void noChildAutoDone(Context context) {
        if (CollectionUtils.isEmpty(children)) done(context);
    }

    public int nextIndex(Context context) {
        return 1;
    }

    public void done(Context context) {
        context.removeCurrent(this);
        int offset = nextIndex(context);
        setCurrentState(context, NodeState.Done);
        if (parent != null) {
            if (!this.hasBrother(offset) || parent instanceof IParallel) {
                parent.done(context);
                return;
            } else {
                getBrother(offset).doing(context);
            }
        }
    }

    protected void setCurrentState(Context context, NodeState state) {
        context.getCurrent().setState(state);
    }


    public boolean hasBrother(int offset) {
        if (this.parent != null) {
            int index = this.parent.children.indexOf(this);
            return index >= 0 && index + offset >= 0 && index + offset < this.parent.children.size();
        }
        return false;
    }

    public BaseNode getBrother(int offset) {
        if (hasBrother(offset)) {
            int index = this.parent.children.indexOf(this);
            return this.parent.children.get(index + offset);
        }
        return null;
    }


    public void undoing(Context context) {
        context.getCurrent().setState(NodeState.RollBacking);
        int index = context.getPaths().size() - 1;
        Path path = context.getPaths().get(index);
        Assert.isTrue(Objects.equals(path.getNode(), this), "错误的操作！");
        context.getPaths().remove(index);
        context.getCurrent().setState(NodeState.Rollback);
        undone(context);
    }

    public void undone(Context context) {
        int index = context.getPaths().size() - 1;
        if (context.current == context.getPaths().get(index)) {
            return;
        }
        context.getPaths().get(index).getNode().undone(context);
    }


    public String getId() {
        String id = attr.get("id");
        BaseNode node = this;
        do {
            if (node instanceof IRoot) {
                id = node.getId() + "@" + id;
                break;
            }
            node = node.parent;
        } while (node != null);
        return id;
    }

    protected <T> T getAttr(String key, Class<T> type) {
        String t = this.getAttr(key);
        if (StringUtils.isEmpty(t)) {
            return null;
        } else if (type.isPrimitive()) {
            return TypeUtils.cast(t, type, ParserConfig.getGlobalInstance());
        } else {
            return TypeUtils.cast(JSON.toJSON(t), type, ParserConfig.getGlobalInstance());
        }
    }

    protected String getAttr(String key) {
        return attr.get(key);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + attr;
    }

    public String toTagName(boolean start) {
        return toTagName(start, true);
    }

    protected String toTagName(boolean start, boolean head) {
        StringBuilder builder = new StringBuilder();
        if (parent != null) {
            builder.append(parent.toTagName(true, false));
        }
        if (!start)
            builder.append(head ? "<-[结束]--" : "<--------");
        else
            builder.append(head ? "--[开始]->" : "-------->");
        builder.append("[").append(this.getClass().getSimpleName()).append(":").append(attr.get("id")).append("]");
        /*if (showAttr)
            for (Map.Entry<String, String> entry : attr.entrySet()) {
                if (entry.getKey().equals("id")) continue;
                builder.append(" ").append(entry.getKey()).append("=\"").append(entry.getValue()).append("\"");
            }*/
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BaseNode baseNode = (BaseNode) o;
        return Objects.equals(getId(), baseNode.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }
}
