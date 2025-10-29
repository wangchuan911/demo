package org.welisdoon.metadata.prototype.handle.link.construction.sql.entity;

import org.welisdoon.metadata.prototype.consts.LinkMetaType;
import org.welisdoon.metadata.prototype.define.MetaLink;
import org.welisdoon.metadata.prototype.entity.DataObject;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @Classname Sql
 * @Description TODO
 * @Author Septem
 * @Date 11:12
 */
public abstract class Sql extends MetaLink {

    String getPrefix() {
        MetaLink metaLink = getParent();
        if (metaLink instanceof Sql)
            return ((Sql) metaLink).getPrefix();
        return "";
    }

    protected void readonly() {
        this.setState(LifeState.Readonly);
    }

    abstract protected void build();

    abstract protected String format(FormatContent content);

    public static class FormatContent {
        List<Part> tableParts = new LinkedList<>();
        List<FormatColumn> columnParts = new LinkedList<>();
        int level = 0;

        public void deep() {
            level++;
        }

        public void shallow() {
            level--;
        }

        public int getTableCount() {
            return tableParts.size();
        }

        public void addTablePart(Part part) {
            tableParts.add(part);
        }

        public void addColumnPart(FormatColumn sqlAlias) {
            columnParts.add(sqlAlias);
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

        protected List<FormatColumn> getColumnParts() {
            return columnParts;
        }

        protected List<Part> getTableParts() {
            return tableParts;
        }

        public static class Part extends SqlAlias {
            LinkMetaType parentType;
            LinkMetaType type;
            List<String> condition;
            int level;

            public Part(FormatContent content, SqlAlias sqlAlias, List<String> condition) {
                super(sqlAlias.getAlias(), sqlAlias.getTarget());
                this.level = content.level;
                this.condition = condition == null ? List.of() : condition;
            }


            public Part setType(LinkMetaType type) {
                this.type = type;
                return this;
            }

            public Part setParentType(LinkMetaType parentType) {
                this.parentType = parentType;
                return this;
            }

            public LinkMetaType getParentType() {
                return parentType;
            }

            public int getLevel() {
                return level;
            }

            public LinkMetaType getType() {
                return type;
            }

            public List<String> getCondition() {
                return condition;
            }
        }
    }
}
