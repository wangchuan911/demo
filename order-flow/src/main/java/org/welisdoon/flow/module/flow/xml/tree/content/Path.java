package org.welisdoon.flow.module.flow.xml.tree.content;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.ParserConfig;
import com.alibaba.fastjson.util.TypeUtils;
import org.welisdoon.flow.module.flow.xml.tree.enums.NodeState;
import org.welisdoon.flow.module.flow.xml.tree.handler.SAXParserHandler;

import java.util.Date;
import java.util.Objects;

/**
 * @Classname ContentHistory
 * @Description TODO
 * @Author Septem
 * @Date 15:06
 */
public class Path {
    final BaseNode node;
    Date startTime;
    Date endTime;
    Object params;
    Object result;
    NodeState state = NodeState.Doing;
    final Context context;

    public Path(Context context, BaseNode node, Object params) {
        this(context, node);
        this.params = params;
    }

    public Path(Context context, BaseNode node) {
        this.context = context;
        this.node = node;
    }

    public Path(Context context, String nodeId, Object params) {
        this(context, toNode(nodeId), params);
    }

    public Path(Context context, String nodeId) {
        this(context, toNode(nodeId));
    }

    public static BaseNode toNode(String nodeId) {
        String[] id1 = nodeId.split(";");
        String id2 = id1[id1.length - 1];
        if (id2.contains("@")) {
            String[] id3 = id2.split("@");
            return SAXParserHandler.findNode(SAXParserHandler.getFlow(id3[0]), id2);
        } else {
            return SAXParserHandler.getFlow(nodeId);
        }
    }

    public void setState(NodeState state) {
        this.state = state;
    }

    public NodeState getState() {
        return state;
    }

    public BaseNode getNode() {
        return node;
    }


    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setResult(Object result) {
        this.result = result;
    }

    public <T> T getResult(Class<T> aClass) {
        if (result == null) {
            return null;
        } else if (aClass.isAssignableFrom(result.getClass())) {
            return (T) result;
        }
        return TypeUtils.cast(JSON.toJSON(result), aClass, ParserConfig.getGlobalInstance());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Path path = (Path) o;
        return Objects.equals(getNode(), path.getNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNode());
    }
}
