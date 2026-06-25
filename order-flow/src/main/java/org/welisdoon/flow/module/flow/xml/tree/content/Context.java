package org.welisdoon.flow.module.flow.xml.tree.content;

import org.welisdoon.common.LogUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @Classname Content
 * @Description TODO
 * @Author Septem
 * @Date 17:55
 */
public class Context {
    String id = "" + System.currentTimeMillis();
    Path current;
    List<Path> paths = new LinkedList<>();
    Context parent;
    List<Context> children = new LinkedList<>();


    public Path getCurrent() {
        return current;
    }

    public Path getCurrentBrother(int offset) {
        int index = paths.indexOf(current);
        if (index < 0) return null;
        index += offset;
        if (index < 0 || index >= paths.size()) return null;
        return paths.get(index);
    }


    public Context[] copy(int count) {
        Context[] contexts = new Context[count];
        for (int i = 0; i < count; i++) {
            contexts[i] = new Context();
            contexts[i].parent = this;
            Path path = getCurrent();
            Path path2 = new Path(contexts[i], path.node, path.params);
            path2.setState(path.getState());
            contexts[i].current = path2;
            contexts[i].paths.add(path2);
            this.children.add(contexts[i]);
        }
        return contexts;
    }

    public List<Path> getPaths() {
        return paths;
    }

    public Context getParent() {
        return parent;
    }

    public List<Context> getChildren() {
        return children;
    }

    public void addCurrent(BaseNode node) {
        current = new Path(this, node, this);
        paths.add(current);

        System.out.println(LogUtils.styleString("", 32, 1, node.toTagName(true)));
        System.out.println(this.getFullId() + "--" + paths.stream().map(path -> path.getNode()).collect(Collectors.toList()));

    }

    public void removeCurrent(BaseNode node) {
        for (int index = paths.size() - 1; index >= 0; index--) {
            Path path = paths.get(index);
            if (Objects.equals(path.getNode(), node)) {
                current = path;
                for (; index < paths.size(); ) {
                    paths.remove(index);
                }
                System.out.println(this.getFullId() + "--" + paths.stream().map(Path::getNode).collect(Collectors.toList()));
                System.out.println(LogUtils.styleString("", 31, 1, node.toTagName(false)));
                return;
            }
        }

    }

    public String getFullId() {
        if (parent != null) {
            return parent.getFullId() + "-" + id;
        }
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Context context = (Context) o;
        return Objects.equals(id, context.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
