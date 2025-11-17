package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.springframework.util.Assert;
import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaPrototype;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.util.LinkedList;
import java.util.List;

/**
 * @Classname FormatContent
 * @Description TODO
 * @Author Septem
 * @Date 15:15
 */
public class FormatContent {
    final protected VirtualPart tablePart;
    private LinkedList<Part> current = new LinkedList<>();

    public FormatContent() {
        tablePart = new VirtualPart(this, LinkMetaType.ObjConstructor);
        current.addLast(tablePart);
    }

    public int getTableCount() {
        return tablePart.getChildren().size();
    }

    public void addTablePart(Part part) {
        Assert.isTrue(current.getLast() instanceof VirtualPart, "上级必须为虚拟");
        if (part instanceof VirtualPart) {
            if (current.size() > 0) {
                ((VirtualPart) current.getLast()).addChildren(part);
            }
            current.addLast(part);
            return;
        }
        ((VirtualPart) current.getLast()).addChildren(part);
    }

    public void pullTablePart() {
        current.removeLast();
    }

    public void addColumnPart(Part.FormatColumn sqlAlias) {
        current.getLast().columns.add(sqlAlias);
    }


//        protected List<Part.FormatColumn> getColumnParts() {
//            return columnParts;
//        }

    protected List<Part> getTablePart() {
        return tablePart.getChildren();
    }

    public static class VirtualPart extends Part implements MetaPrototype.Parent<Part> {

        final List<Part> children = new LinkedList<>();

        public VirtualPart(FormatContent content, LinkMetaType type) {
            super(content, new SqlAlias(String.valueOf(System.currentTimeMillis()), ""), List.of(), type, List.of());
        }

        @Override
        public List<Part> getChildren() {
            return children;
        }
    }

    public static class LeafPart extends Part {
        public LeafPart(FormatContent content, SqlAlias sqlAlias, List<String> condition, List<FormatColumn> column, LinkMetaType type) {
            super(content, sqlAlias, condition, type, column);
        }
    }

    public static class Part extends SqlAlias implements MetaPrototype.Child<Part> {
        final LinkMetaType type;
        final List<String> condition;
        final List<FormatColumn> columns;
        Part parent;

        protected Part(FormatContent content, SqlAlias sqlAlias, List<String> condition, LinkMetaType type, List<FormatColumn> column) {
            super(sqlAlias.getAlias(), sqlAlias.getTarget());
            this.condition = condition == null ? List.of() : condition;
            this.type = type;
            this.columns = column;
        }


        public LinkMetaType getType() {
            return type;
        }

        public List<String> getCondition() {
            return condition;
        }

        public List<FormatColumn> getColumns() {
            return columns;
        }


        public int getLevel() {
            int level = 0;
            Part p = this;
            while ((p = p.getParent()) != null) {
                level++;
            }
            return level;
        }

        public boolean isGroup(Part part) {
            if (part.equals(this)) {
                return true;
            }
            int target = part.getLevel();
            Part pTarget = part;
            int self = this.getLevel();
            Part pSelf = this;
            if (target > self) {
                pTarget = pTarget.getParent();
                target--;

            } else if (self > target) {
                pSelf = pSelf.getParent();
                self--;
            }
            if (pTarget.equals(pSelf)) {
                return true;
            }
            for (int i = target - 1; i > 1; i--) {
                if (pTarget.getParent().equals(pSelf.getParent())) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Part getParent() {
            return parent;
        }

        @Override
        public MetaPrototype.Child setParent(Part parent) {
            return this;
        }

        public static class FormatColumn extends SqlAlias {
            DataObject.Field field;

            public FormatColumn(String alias, String target, DataObject.Field field) {
                super(alias, target);
                this.field = field;
            }

            public DataObject.Field getField() {
                return field;
            }
        }
    }
}
